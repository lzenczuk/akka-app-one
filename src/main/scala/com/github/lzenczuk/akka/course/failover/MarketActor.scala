package com.github.lzenczuk.akka.course.failover

import akka.actor.{Actor, ActorLogging, Props}
import com.github.lzenczuk.akka.course.failover.MarketActor.{DoNothingMarket, FailMarket, MarketException, StopMarket}

/**
  * Created by dev on 18/08/16.
  */
object MarketActor {
  sealed trait MarketCommand
  case class FailMarket(message:String) extends MarketCommand
  case class StopMarket(message:String) extends MarketCommand
  case class DoNothingMarket(message:String) extends MarketCommand

  case class MarketException(message:String) extends Exception

  def props(id:Long) = Props(new MarketActor(id))
}

class MarketActor(id:Long) extends Actor with ActorLogging {

  override def receive = {
    case StopMarket(msg) =>
      context stop self
      log.info(s"Market $id stopped: $msg")
    case FailMarket(msg) =>
      log.info(s"Market $id throwing exception: $msg")
      throw MarketException(msg)
    case DoNothingMarket(msg) =>
      log.info(s"Market $id do nothing: $msg")
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    log.info(s"Marked $id stopped.")
  }

  @scala.throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    log.info(s"Market $id restarted.")
  }
}
