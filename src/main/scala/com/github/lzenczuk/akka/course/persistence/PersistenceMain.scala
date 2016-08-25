package com.github.lzenczuk.akka.course.persistence

import java.util.Date

import akka.NotUsed
import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence._
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.github.lzenczuk.akka.course.persistence.PersistentCounter.{DecreaseCommand, IncreaseCommand}

/**
  * Created by dev on 24/08/16.
  */

object PersistentCounter {
  case class IncreaseCommand(value:Int)
  case class DecreaseCommand(value:Int)

  case class IncreaseEvent(value:Int)
  case class DecreaseEvent(value:Int)

  case class State(value:Int, created:Long){
    // Create new state using copy method of case class that lest to create new instance changing only some fields
    def inc(v:Int):State = copy(value = value+v)
    def dec(v:Int):State = copy(value = value-v)
  }
}

class PersistentCounter(id:Long) extends PersistentActor with ActorLogging {
  import PersistentCounter._

  log.info("Start")

  var state:State = State(0, new Date().getTime)
  var changesCounter = 0

  override def persistenceId: String = s"counter-$id"

  // Receive Events in recovery mode
  override def receiveRecover: Receive = {
    case IncreaseEvent(v) =>
      state = state.inc(v)
      log.info(s"State: $state")
    case DecreaseEvent(v) =>
      state = state.dec(v)
      log.info(s"State: $state")
    case SnapshotOffer(metadata, snapshotState:State) =>
      log.info(s"Receive snapshot offer: $metadata")
      state = snapshotState
    case RecoveryCompleted =>
      log.info("Recovery completed!")
  }

  // Receive Commands in normal mode
  override def receiveCommand: Receive = {
    case IncreaseCommand(v) =>
      log.info(s"Increase command: $v")
      persist(IncreaseEvent(v)){
        event =>  state = state.inc(v)
          log.info(s"State: $state")
          changesCounter+=1
          if(changesCounter>3) {
            log.info("Take snapshot")
            saveSnapshot(state)
            changesCounter=0
          }
      }
    case DecreaseCommand(v) =>
      log.info(s"Decrease command: $v")
      persist(DecreaseEvent(v)){
        event =>  state = state.dec(v)
          log.info(s"State: $state")
          changesCounter+=1
          if(changesCounter>3) {
            log.info("Take snapshot")
            saveSnapshot(state)
            changesCounter=0
          }
      }
    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Snapshot saved $metadata.")
    case SaveSnapshotFailure(metadata, reason) =>
      log.error(s"Snapshot save failed $metadata: $reason")
  }
}

object PersistenceMain extends App{
  private val system: ActorSystem = ActorSystem("persistent-counter-system")

  private val counter: ActorRef = system.actorOf(Props(new PersistentCounter(5)))

  counter ! IncreaseCommand(10)
  counter ! IncreaseCommand(6)
  counter ! DecreaseCommand(11)
  counter ! IncreaseCommand(1)
  counter ! DecreaseCommand(17)
  counter ! DecreaseCommand(1)
  counter ! IncreaseCommand(6)
  counter ! IncreaseCommand(15)
  counter ! DecreaseCommand(7)
  counter ! DecreaseCommand(2)

  Thread.sleep(3000L)
  system.terminate()
}

object PersistentCounterQueryMain extends App {
  private val system: ActorSystem = ActorSystem("market-persistence-system")
  implicit val mat = ActorMaterializer()(system)

  private val queries: LeveldbReadJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
  private val source: Source[EventEnvelope, NotUsed] = queries.eventsByPersistenceId("counter-5", 0L, Long.MaxValue)

  source.runForeach{e => println(s"Event: $e")}

}