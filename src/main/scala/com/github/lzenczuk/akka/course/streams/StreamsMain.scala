package com.github.lzenczuk.akka.course.streams

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by dev on 30/08/16.
  */

object NumberProcessingActor {
  case object StreamCompleted
}

class NumberProcessingActor extends Actor with ActorLogging {

  override def preStart = {
    log.info("Actor pre start")
  }

  def receive = {
    case n:Int =>
      log.info(s"Receive number: $n")
    case NumberProcessingActor.StreamCompleted =>
      log.info("Stream completed. Terminating actor system")
      //context.system.terminate()
  }
}

object StreamsMain extends App{
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  private val source: Source[Int, NotUsed] = Source(1 to 100)
  private val addTen: Flow[Int, Int, NotUsed] = Flow[Int].map(_+10)
  private val subTen: Flow[Int, Int, NotUsed] = Flow[Int].map(_-10)
  // private val sink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
  private val actorSink: Sink[Any, NotUsed] = Sink.actorRef(system.actorOf(Props[NumberProcessingActor]), NumberProcessingActor.StreamCompleted)

  println("Stream definition")
  source.via(addTen).via(subTen).runWith(actorSink)
  println("Main ended")

  private val runnableGraph: RunnableGraph[NotUsed] = Source(1 to 10)
    .map(_+2)
    .map(_-1)
    .alsoTo(Sink.foreach(n => println(s"Stream 1: $n")))
    .to(Sink.foreach(n => println(s"Stream 2: $n")))
  runnableGraph.run()

  Thread.sleep(1000L)
  system.terminate()
}
