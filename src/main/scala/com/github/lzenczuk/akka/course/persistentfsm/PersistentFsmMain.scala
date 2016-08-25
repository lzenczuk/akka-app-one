package com.github.lzenczuk.akka.course.persistentfsm

import java.util.Date

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, FSM, Props}
import akka.persistence.fsm._
import akka.persistence.fsm.PersistentFSM.FSMState
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.github.lzenczuk.akka.course.persistentfsm.MarketFsm._

import scala.reflect._

/**
  * Created by dev on 24/08/16.
  */

object MarketFsm {

  sealed trait State extends FSMState
  case object Pending extends State {
    override def identifier: String = "pending"
  }
  case object Open extends State {
    override def identifier: String = "open"
  }
  case object Suspended extends State {
    override def identifier: String = "suspended"
  }
  case object Close extends State {
    override def identifier: String = "closed"
  }
  case object Grade extends State {
    override def identifier: String = "graded"
  }

  case class MarketChange(date:Date, changeType:String, reason:Option[String])
  case class Market(name:String, history:List[MarketChange])

  sealed trait Command
  case object OpenMarketCommand extends Command
  case object CloseMarketCommand extends Command
  case class SuspendMarketCommand(reason:String) extends Command
  case object GradeMarketCommand extends Command
  case object ShowMarketCommand extends Command

  sealed trait Event
  case object OpenMarketEvent extends Event
  case object CloseMarketEvent extends Event
  case class SuspendMarketEvent(reason:String) extends Event
  case object GradeMarketEvent extends Event
}

class MarketFsm(name:String, id:Long) extends PersistentFSM[MarketFsm.State, MarketFsm.Market, MarketFsm.Event]{
  import MarketFsm._

  override def persistenceId: String = s"market-$id"

  override def applyEvent(evt: MarketFsm.Event, market: Market): Market = {
    evt match {
      case OpenMarketEvent => market.copy(history = MarketChange(new Date(), "opened", None) :: market.history)
      case CloseMarketEvent => market.copy(history = MarketChange(new Date(), "closed", None) :: market.history)
      case SuspendMarketEvent(reason) => market.copy(history = MarketChange(new Date(), "closed", Some(reason)) :: market.history)
      case GradeMarketEvent => market.copy(history = MarketChange(new Date(), "graded", None) :: market.history)
    }
  }

  override def domainEventClassTag: ClassTag[MarketFsm.Event] = classTag[MarketFsm.Event]

  startWith(Pending, Market(name, List(MarketChange(new Date(), "pending", None))))

  when(Pending){
    case Event(OpenMarketCommand, market) =>
      goto(Open) applying OpenMarketEvent
    case Event(CloseMarketCommand, market) =>
      goto(Close) applying CloseMarketEvent
    case Event(SuspendMarketCommand(_), market) =>
      log.error("Suspend market. Incorrect command. Can't suspend pending market.")
      stay()
    case Event(GradeMarketCommand, market) =>
      log.error("Grade market. Incorrect command. Can't grade pending market.")
      stay()
    case Event(ShowMarketCommand, market) =>
      log.info(s"Market: $market")
      stay()
  }

  when(Open){
    case Event(OpenMarketCommand, market) =>
      log.error("Open market. Incorrect command. Can't open already opene market.")
      stay()
    case Event(CloseMarketCommand, market) =>
      goto(Close) applying CloseMarketEvent
    case Event(SuspendMarketCommand(reason), market) =>
      goto(Suspended) applying SuspendMarketEvent(reason)
    case Event(GradeMarketCommand, market) =>
      log.error("Grade market. Incorrect command. Can't grade open market.")
      stay()
    case Event(ShowMarketCommand, market) =>
      log.info(s"Market: $market")
      stay()
  }

  when(Suspended){
    case Event(OpenMarketCommand, market) =>
      goto(Open) applying OpenMarketEvent
    case Event(CloseMarketCommand, market) =>
      goto(Close) applying CloseMarketEvent
    case Event(SuspendMarketCommand(reason), market) =>
      log.error("Suspend market. Incorrect command. Can't suspend already suspended market.")
      stay()
    case Event(GradeMarketCommand, market) =>
      log.error("Grade market. Incorrect command. Can't grade suspended market.")
      stay()
    case Event(ShowMarketCommand, market) =>
      log.info(s"Market: $market")
      stay()
  }

  when(Close){
    case Event(OpenMarketCommand, market) =>
      goto(Open) applying OpenMarketEvent
    case Event(CloseMarketCommand, market) =>
      log.error("Close market. Incorrect command. Can't close already closed market.")
      stay()
    case Event(SuspendMarketCommand(reason), market) =>
      log.error("Suspend market. Incorrect command. Can't suspend closed market.")
      stay()
    case Event(GradeMarketCommand, market) =>
      goto(Grade) applying GradeMarketEvent
    case Event(ShowMarketCommand, market) =>
      log.info(s"Market: $market")
      stay()
  }

  when(Grade){
    case Event(OpenMarketCommand, market) =>
      log.error("Open market. Incorrect command. Can't open graded market.")
      stay()
    case Event(CloseMarketCommand, market) =>
      log.error("Close market. Incorrect command. Can't close graded market.")
      stay()
    case Event(SuspendMarketCommand(_), market) =>
      log.error("Suspend market. Incorrect command. Can't suspend graded market.")
      stay()
    case Event(GradeMarketCommand, market) =>
      log.error("Grade market. Incorrect command. Can't grade already graded market.")
      stay()
    case Event(ShowMarketCommand, market) =>
      log.info(s"Market: $market")
      stay()
  }
}

object PersistentFsmMain extends App{
  private val system: ActorSystem = ActorSystem("market-persistence-system")

  private val marketActor: ActorRef = system.actorOf(Props(new MarketFsm("test1", 2)), "market_actor")

  marketActor ! ShowMarketCommand
  marketActor ! OpenMarketCommand
  marketActor ! ShowMarketCommand
  marketActor ! OpenMarketCommand
  marketActor ! SuspendMarketCommand("Suspend 1")
  marketActor ! ShowMarketCommand
  marketActor ! CloseMarketCommand
  marketActor ! ShowMarketCommand
  marketActor ! SuspendMarketCommand("This won't work. Can't suspend closed market.")
  marketActor ! OpenMarketCommand
  marketActor ! SuspendMarketCommand("Second suspension")
  marketActor ! CloseMarketCommand
  marketActor ! GradeMarketCommand
  marketActor ! ShowMarketCommand

  Thread.sleep(3000L)
  system.terminate()
}

object PersistentQueryMain extends App {
  private val system: ActorSystem = ActorSystem("market-persistence-system")
  implicit val mat = ActorMaterializer()(system)

  private val queries: LeveldbReadJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
  private val source: Source[EventEnvelope, NotUsed] = queries.eventsByPersistenceId("market-2", 0L, Long.MaxValue)

  source.runForeach{e => println(s"Event: $e")}

}