package com.github.lzenczuk.akka.course

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, ActorSystem, Identify, Props}
import com.github.lzenczuk.akka.course.EventActor.{CreateMarket, DestroyMarket}
import com.github.lzenczuk.akka.course.MarketActor.{MarketStopped, Stop, SubmitOffer}
import com.github.lzenczuk.akka.course.MarketsObserver.UpdateMarketsView

import scala.util.Random

/**
  * Created by dev on 18/08/16.
  */
object SupervisingAndMonitoringMain extends App{
  private val system: ActorSystem = ActorSystem("betting-actor-system")

  private val event1: ActorRef = system.actorOf(Props[EventActor],"Event_1")
  private val event2: ActorRef = system.actorOf(Props[EventActor],"Event_2")
  private val marketObserver: ActorRef = system.actorOf(Props[MarketsObserver])

  event1 ! CreateMarket("Market_1")
  event1 ! CreateMarket("Not_market")

  event2 ! CreateMarket("Market_1")
  event2 ! CreateMarket("Not_market")

  Thread.sleep(200L)
  system.actorSelection("/user/*") ! GlobalMessage("Message 1")
  system.actorSelection("/user/Event_1/*") ! GlobalMessage("Message 2")
  system.actorSelection("/user/Event_1/Market_*") ! GlobalMessage("Message 3")

  marketObserver ! UpdateMarketsView()
  Thread.sleep(200L)

  event1 ! DestroyMarket("Market_1")
  Thread.sleep(200L)
  system.actorSelection("/user/*") ! GlobalMessage("Message 4")

  marketObserver ! UpdateMarketsView()

  Thread.sleep(200L)
  println("Shutdown")
  system.shutdown()
}

case class GlobalMessage(message:String)

object EventActor {
  sealed trait EventCommands
  case class CreateMarket(marketName:String) extends EventCommands
  case class DestroyMarket(marketName:String) extends EventCommands
}

object MarketActor {
  sealed trait MarketCommands
  case class SubmitOffer(stake:Int) extends MarketCommands
  case class Stop() extends MarketCommands
  case class MarketStopped() extends MarketCommands

  def props(marketName:String) = Props(new MarketActor(marketName))
}

class EventActor extends Actor with ActorLogging{

  var markets = Map.empty[String, ActorRef]

  override def receive = {
    case CreateMarket(marketName) =>
      log.info(s"Creating market: $marketName")
      markets += (marketName -> context.actorOf(MarketActor.props(marketName), marketName))
      log.info(s"Market: $marketName created")

    case DestroyMarket(marketName) =>
      log.info("Destroing market: "+marketName)
      if(markets.contains(marketName)){
        markets(marketName) ! Stop()
      }else{
        log.info(s"Market $marketName not exists.")
      }
    case MarketStopped() => log.info("Market stopped.")
    case GlobalMessage(msg) => log.info(s"Event received global message: $msg")
  }


}

class MarketActor(name:String) extends Actor with ActorLogging{

  var stopCommandSender:ActorRef = _

  override def receive = {
    case SubmitOffer(stake) => log.info("Submit offer: "+stake)
    case Stop() =>
      stopCommandSender = sender()
      log.info(s"Stop sender $stopCommandSender")
      context.stop(self)
    case GlobalMessage(msg) => log.info(s"Market $name received global message: $msg")
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop() = {
    log.info(s"Stopping market $name")

    if(stopCommandSender!=null){
      stopCommandSender ! MarketStopped()
    }
  }
}

object MarketsObserver {
  sealed trait MarketsObserverCommand
  case class UpdateMarketsView() extends MarketsObserverCommand
}

class MarketsObserver extends Actor with ActorLogging {

  val marketsObserverId = Random.nextLong()

  override def receive = {
    case UpdateMarketsView() =>
      context.actorSelection("/user/Event_*/Market_*") ! Identify(marketsObserverId)
    case ActorIdentity(correlationId, Some(ref)) if correlationId==marketsObserverId =>
      log.info(s"Found actor ${ref}")
      println(s"Actor path ${ref.path}")
      println(s"Actor path root ${ref.path.root}")
      println(s"Actor path address ${ref.path.address}")
      println(s"Actor path name ${ref.path.name}")
      println(s"Actor path parent ${ref.path.parent}")
    case ActorIdentity(correlationId, None) =>
      log.info(s"Actor not found ${correlationId}")
  }
}
