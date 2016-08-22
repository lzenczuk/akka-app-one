package com.github.lzenczuk.akka.course.become

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash}
import akka.actor.Actor.Receive
import com.github.lzenczuk.akka.course.become.User.{Message, OffLine, OnLine}

/**
  * Created by dev on 22/08/16.
  */

object User {
  sealed trait UserCommands
  case object OnLine extends UserCommands
  case object OffLine extends UserCommands
  case class Message(content:String) extends UserCommands
}

class User(name:String) extends Actor with ActorLogging with Stash{

  def receive = offLint

  def onLint:Actor.Receive = {
    case OffLine =>
      log.info(s"User $name off-line")
      context.unbecome()
    case Message(msg) =>
      log.info(s"User $name receive message: $msg")
  }

  def offLint:Actor.Receive = {
    case OnLine =>
      log.info(s"User $name on-line")
      unstashAll()
      context.become(onLint)
    case Message(_) =>
      log.info(s"User $name off-line. Stash message to consume later.")
      stash()
  }

}

object BecomeMain extends App{
  private val system: ActorSystem = ActorSystem("user-message-system")

  private val user1: ActorRef = system.actorOf(Props(new User("Mark")))

  user1 ! Message("Hi Mark")
  user1 ! Message("How are you?")
  user1 ! OffLine
  user1 ! Message("Boom boom boom!")
  user1 ! OnLine
  user1 ! Message("UR on line!")
  user1 ! OffLine
  user1 ! Message("Off line?")

  Thread.sleep(200L)
  system.shutdown()
}
