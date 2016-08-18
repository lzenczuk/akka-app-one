package com.github.lzenczuk.akka.course.failover

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorSelection, Identify, Terminated}

/**
  * Created by dev on 18/08/16.
  */
class MarketsObserver extends Actor with ActorLogging{


  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    context.actorSelection("/user/*/Market_*") ! Identify()
  }

  override def receive = {
    case ActorIdentity(_, Some(ref)) =>
      log.info(s"Receive market ref: ${ref.path}")
      context.watch(ref)
    case Terminated(ref) =>
      log.info(s"Market ${ref.path} terminated.")
      context.unwatch(ref)
    case x:Any =>
      log.info(s"Receive message: $x")
  }
}
