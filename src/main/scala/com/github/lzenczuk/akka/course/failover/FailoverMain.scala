package com.github.lzenczuk.akka.course.failover

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import com.github.lzenczuk.akka.course.failover.EventActor.CreateMarket
import com.github.lzenczuk.akka.course.failover.MarketActor.{DoNothingMarket, FailMarket}

/**
  * Created by dev on 18/08/16.
  */
object FailoverMain extends App{

  private val system: ActorSystem = ActorSystem("failover-actor-system")

  private val eventActor: ActorRef = system.actorOf(Props[EventActor],"Event")

  eventActor ! CreateMarket()
  eventActor ! CreateMarket()

  Thread.sleep(200L)
  system.actorOf(Props[MarketsObserver], "MarketsObserver")

  Thread.sleep(200L)
  println("Single exception")

  private val markets: ActorSelection = system.actorSelection("/user/Event/Market_*")
  markets ! DoNothingMarket("Do nothing message 1")
  markets ! FailMarket("Fail market message 2")
  markets ! DoNothingMarket("Do nothing message 3")

  Thread.sleep(1000L)
  println("Multiple exceptions")
  markets ! FailMarket("Fail market message 10")
  markets ! FailMarket("Fail market message 11")
  markets ! FailMarket("Fail market message 12")
  markets ! FailMarket("Fail market message 13")
  markets ! FailMarket("Fail market message 14")
  markets ! FailMarket("Fail market message 15")
  markets ! FailMarket("Fail market message 16")

  Thread.sleep(1000L)
  println("Shutdown")
  system.shutdown()
}
