package com.github.lzenczuk.akka.course.httpclient

import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Framing, RunnableGraph, Sink, Source}
import akka.util.ByteString

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by dev on 01/09/16.
  */
object HttpClient extends App{
  implicit private val system: ActorSystem = ActorSystem("http-client-actor-system")
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  // Dublin osm: https://s3.amazonaws.com/metro-extracts.mapzen.com/dublin_ireland.osm.bz2

  /*private val connectionFlow: Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = Http().outgoingConnection("s3.amazonaws.com")
    .map{
      case r @ HttpResponse(StatusCodes.OK, headers, entity, _) =>
        println("Saving data to file")
        entity.withoutSizeLimit().dataBytes.to(FileIO.toPath(Paths.get("out.osm"))).run()
        println("Stream started")
        r
      case r @ HttpResponse(code, _, _, _) =>
        println(s"Http error: $code")
        r
    }
    */

  /*private val source1: Source[Int, NotUsed] = Source(1 to 10)
  private val flow1: Flow[Int, Int, NotUsed] = Flow[Int].map(-_)
  private val sourcesFromSourceAndFlow: Source[Int, NotUsed] = source1.via(flow1)

  private val source2: Source[Int, NotUsed] = Source(1 to 10)
  private val sink2: Sink[Int, Future[Int]] = Sink.reduce((n1:Int, n2:Int) => n1+n2)
  private val runableGraphFromSourceAndSink: RunnableGraph[NotUsed] = source2.to(sink2)

  private val flow3: Flow[Int, Int, NotUsed] = Flow[Int].map(-_)
  private val sink3: Sink[Int, Future[Int]] = Sink.reduce((n1:Int, n2:Int) => n1+n2)
  private val sinkFromFlowAndSink: Sink[Int, NotUsed] = flow3.to(sink3)

  private val flow41: Flow[Int, Int, NotUsed] = Flow[Int].map(-_)
  private val flow42: Flow[Int, Int, NotUsed] = Flow[Int].map(_*2)
  private val flowFromTwoFlows: Flow[Int, Int, NotUsed] = flow41.via(flow42)

  private val runnableGraph: RunnableGraph[NotUsed] = sourcesFromSourceAndFlow.via(flowFromTwoFlows).to(sinkFromFlowAndSink)

  runnableGraph.run()
  runableGraphFromSourceAndSink.run()

  private val optionalSource: Source[Int, Promise[Option[Int]]] = Source.maybe[Int]

  private val promise: Promise[Int] = Promise()
  promise.success(10)

  promise.*/

  private val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("out.osm"))
}
