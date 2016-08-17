package com.github.lzenczuk.akka.course

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by dev on 17/08/16.
  */

case class GreetMessage(who:String)
case class End()

class GreetActor extends Actor {

  // receive method returns function that consume message
  override def receive = {
    case GreetMessage(who) => println("Hello "+who)
    case End() => {
      println("Time to sey goodbye")
      context.system.shutdown()
    }
  }

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    println("Preparing actor to start")
    println("Do sth")
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    println("Actor stopped")
    println("Clean up post stop")
  }
}

object CourseMain extends App{
  println("Hello CourseMain")

  private val system: ActorSystem = ActorSystem("CourseActorSystem")
  private val greetActorRef: ActorRef = system.actorOf(Props[GreetActor])

  greetActorRef ! GreetMessage("Mark")
  greetActorRef ! GreetMessage("Tom")
  greetActorRef ! End()
}
