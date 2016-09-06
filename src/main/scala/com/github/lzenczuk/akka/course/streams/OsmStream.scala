package com.github.lzenczuk.akka.course.streams

import java.io.FileInputStream
import java.nio.charset.Charset

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Framing, RunnableGraph, Sink, Source, StreamConverters}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by dev on 02/09/16.
  */

object OsmStream extends App {
  implicit private val system: ActorSystem = ActorSystem("osm-reading-system")
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  private val uncompressedOsmFileSource: Source[ByteString, Future[IOResult]] =
    StreamConverters.fromInputStream(() => new BZip2MultiStreamCompressorInputStream(new FileInputStream("/home/dev/Documents/osm/dublin/dublin_ireland.osm.bz2")))

  private val bytesToStringLines: Flow[ByteString, String, NotUsed] = Framing.delimiter(ByteString("\n"), 1024).map(bs => {
    bs.decodeString(Charset.defaultCharset())
  })

  val groupNodes: Flow[String, String, NotUsed] = Flow[String]
    .scan(Idle().asInstanceOf[OsmSegment])((segment:OsmSegment, line:String) => segment.parse(line))
    .filter(s => s.isInstanceOf[FullSegment]).map(s => s.content)

  private val osmStream: RunnableGraph[Future[IOResult]] =
    uncompressedOsmFileSource
      .via(bytesToStringLines)
      .via(groupNodes)
      .to(Sink.foreach(line => {println(s"Line: $line")}))

  osmStream.run().andThen{
    case Success(ioResult) =>
      println(s"Success read: ${ioResult.count/1048576} MB")
    case Failure(throwable) => println(s"Error: ${throwable.getMessage}")
  }.andThen{
    case _ => system.terminate()
  }
}
