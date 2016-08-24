package com.github.lzenczuk.akka.course.persistence

import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence._
import com.github.lzenczuk.akka.course.persistence.PersistentCounter.{DecreaseCommand, IncreaseCommand}

/**
  * Created by dev on 24/08/16.
  */

object PersistentCounter {
  case class IncreaseCommand(value:Int)
  case class DecreaseCommand(value:Int)

  case class IncreaseEvent(value:Int)
  case class DecreaseEvent(value:Int)

  case class State(value:Int){
    def inc(v:Int):State = State(value+v)
    def dec(v:Int):State = State(value-v)
  }
}

class PersistentCounter(id:Long) extends PersistentActor with ActorLogging {
  import PersistentCounter._

  log.info("Start")

  var state:State = State(0)
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

  private val counter: ActorRef = system.actorOf(Props(new PersistentCounter(3)))

  counter ! IncreaseCommand(10)
  counter ! IncreaseCommand(6)
  counter ! DecreaseCommand(11)
  counter ! IncreaseCommand(1)
  counter ! DecreaseCommand(17)
  counter ! DecreaseCommand(8)
  counter ! IncreaseCommand(6)
  counter ! IncreaseCommand(3)

  Thread.sleep(3000L)
  system.terminate()
}
