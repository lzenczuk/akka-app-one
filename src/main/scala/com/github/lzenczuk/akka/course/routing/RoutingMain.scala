package com.github.lzenczuk.akka.course.routing

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.routing.{FromConfig, RandomGroup}
import com.github.lzenczuk.akka.course.routing.WorkerActor.WorkerTask

import scala.collection.immutable.Range.Inclusive

/**
  * Created by dev on 22/08/16.
  */


object WorkerActor {
  // TODO - thread safety!!!!!
  var workersCounter = 0

  case class WorkerTask(n:Int)
}

class WorkerActor extends Actor with ActorLogging{
  var workerNumber = WorkerActor.workersCounter
  WorkerActor.workersCounter+=1
  var counter = 0

  override def receive = {
    case WorkerTask(rep) =>
      (0 to rep).foreach(n => log.info(workerNumber +" : "+counter + " -> "+ n))
      counter+=1
  }
}

object RoutingMain extends App{
  private val system: ActorSystem = ActorSystem("random-router-system")

  // Router from configuration
  private val workRouter: ActorRef = system.actorOf(FromConfig.props(Props[WorkerActor]), "random-router-pool")

  workRouter ! WorkerTask(5)
  workRouter ! WorkerTask(11)
  workRouter ! WorkerTask(2)
  workRouter ! WorkerTask(8)
  workRouter ! WorkerTask(3)
  workRouter ! WorkerTask(12)
  workRouter ! WorkerTask(7)

  Thread.sleep(1000L)

  // Router from code
  system.actorOf(Props[WorkerActor], "w1")
  system.actorOf(Props[WorkerActor], "w2")
  system.actorOf(Props[WorkerActor], "w3")

  private val paths: List[String] = List("/user/w1", "/user/w2", "/user/w3")
  private val workRouter2: ActorRef = system.actorOf(RandomGroup(paths).props(), "random-router-grout")

  workRouter2 ! WorkerTask(5)
  workRouter2 ! WorkerTask(11)
  workRouter2 ! WorkerTask(2)
  workRouter2 ! WorkerTask(8)
  workRouter2 ! WorkerTask(3)
  workRouter2 ! WorkerTask(12)
  workRouter2 ! WorkerTask(7)

  Thread.sleep(1000L)
  system.shutdown()
}
