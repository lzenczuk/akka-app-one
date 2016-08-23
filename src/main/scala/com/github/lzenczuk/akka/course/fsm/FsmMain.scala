package com.github.lzenczuk.akka.course.fsm

import akka.actor.{ActorRef, ActorSystem, FSM, Props}
import com.github.lzenczuk.akka.course.fsm.User.Message

/**
  * Created by dev on 23/08/16.
  */

object User {
  sealed trait State
  case object OffLine extends State
  case object OnLine extends State

  sealed trait UserEvent
  case object Connect extends UserEvent
  case object Disconnect extends UserEvent
  case class Message(content:String) extends UserEvent
  case class ShowMessages(prefix:String) extends UserEvent

  type UserData = List[String]
}

class User(name:String) extends FSM[User.State, User.UserData] {
  import User._

  startWith(OffLine, List.empty)

  when(OffLine){
    case Event(Connect, _) =>
      goto(OnLine)
    case Event(Message(msg), messages) =>
      stay using(msg :: messages)

    case Event(ShowMessages(prefix), messages) =>
      log.info(s"$name offline: $prefix -> $messages")
      stay
  }

  import scala.concurrent.duration._

  when(OnLine, stateTimeout = 2 seconds){
    case Event(Message(msg), messages) =>
      stay using(msg :: messages)
    case Event(Disconnect, _) =>
      goto(OffLine)
    case Event(FSM.StateTimeout, _) =>
      log.info("No user activity. Going off-line.")
      goto(OffLine)

    case Event(ShowMessages(prefix), messages) =>
      log.info(s"$name online: $prefix -> $messages")
      stay
  }

  initialize()
}

object FsmMain extends App{
  private val system: ActorSystem = ActorSystem("FSM-app")

  private val userActor1: ActorRef = system.actorOf(Props(new User("Mark")), "user-mark-actor")

  userActor1 ! Message("M1")
  userActor1 ! Message("M2")
  userActor1 ! User.ShowMessages("Initial state after two messages")
  userActor1 ! User.Connect
  userActor1 ! User.ShowMessages("After connect")
  userActor1 ! Message("M3")
  userActor1 ! Message("M4")
  userActor1 ! User.ShowMessages("Next two messages/connected")
  userActor1 ! User.Disconnect
  userActor1 ! User.ShowMessages("After disconnect")
  userActor1 ! Message("M5")
  userActor1 ! Message("M6")
  userActor1 ! User.ShowMessages("Next two messages/disconnected")
  userActor1 ! User.Connect
  userActor1 ! User.ShowMessages("After reconnecting")

  Thread.sleep(2500L)

  userActor1 ! User.ShowMessages("After on line timeout")

  Thread.sleep(500L)
  system.shutdown()
}
