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

  var counter = {
    log.info("Setting counter to 0")
    0
  }

  override def receive = {
    case StopMarket(msg) =>
      context stop self
      log.info(s"Market $id counter $counter stopped: $msg")
      counter += 1
    case FailMarket(msg) =>
      log.info(s"Market $id counter $counter throwing exception: $msg")
      counter += 1
      throw MarketException(msg)
    case DoNothingMarket(msg) =>
      log.info(s"Market $id counter $counter do nothing: $msg")
      counter += 1
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    log.info(s"Marked $id counter $counter post stopped.")
  }


  @scala.throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.info(s"Marked $id counter $counter pre restart.")
  }

  @scala.throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    log.info(s"Market $id counter $counter post restarted.")
  }
}
