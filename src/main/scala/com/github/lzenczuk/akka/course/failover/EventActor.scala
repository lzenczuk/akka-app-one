package com.github.lzenczuk.akka.course.failover

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume}
import akka.actor.{Actor, ActorLogging, AllForOneStrategy, OneForOneStrategy, SupervisorStrategy}
import com.github.lzenczuk.akka.course.failover.MarketActor.MarketException

import scala.util.Random
import scala.concurrent.duration._

/**
  * Created by dev on 18/08/16.
  */
object EventActor {
  sealed trait EventCommand
  case class CreateMarket() extends EventCommand
}

class EventActor extends Actor with ActorLogging {

  import EventActor._
  import scala.math.abs

  override def receive = {
    case CreateMarket() =>
      log.info("creating market")
      val marketId: Long = abs(Random.nextLong())
      context.actorOf(MarketActor.props(marketId), s"Market_$marketId")
  }

  override val supervisorStrategy = AllForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 second, false){
    case MarketException(msg) =>
      log.info(s"Market exception $msg. Restart.")
      Restart
    case t =>
      log.info("Not supported exception")
      super.supervisorStrategy.decider.applyOrElse(t, (_:Any) => Escalate)
  }
}
