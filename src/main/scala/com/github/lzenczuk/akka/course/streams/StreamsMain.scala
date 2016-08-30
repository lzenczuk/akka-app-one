package com.github.lzenczuk.akka.course.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Sink, Source}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by dev on 30/08/16.
  */

object StreamsMain extends App{
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  private val source: Source[Int, NotUsed] = Source(1 to 100)
  private val addTen = Flow[Int].map(_+10)
  private val sink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

  source.via(addTen).runWith(sink).andThen{
    case _ =>
      println("End")
      system.terminate()
  }
}
