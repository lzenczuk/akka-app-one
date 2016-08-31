package com.github.lzenczuk.akka.course.streams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by dev on 30/08/16.
  */

object StreamsGraphMain extends App{
  implicit private val system: ActorSystem = ActorSystem()
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  val g = RunnableGraph.fromGraph(GraphDSL.create(){
    implicit builder: GraphDSL.Builder[NotUsed] =>

      import GraphDSL.Implicits._

      val in: Source[Int, NotUsed] = Source(1 to 10)
      val out: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

      val bcast = builder.add(Broadcast[Int](2))
      val merge = builder.add(Merge[Int](2))

      val f1, f2, f3, f4 = Flow[Int].map(_ + 10)

      in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
      bcast ~> f4 ~> merge

      ClosedShape
  })

  g.run()

  Thread.sleep(1000L)
  println("-----------> end")
  system.terminate()
}
