package readside

import akka.NotUsed
import akka.actor.ExtendedActorSystem
import akka.persistence.query.{EventEnvelope, Offset, ReadJournalProvider}
import akka.stream.scaladsl.Source
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.language.postfixOps

class CassandraReadJournalProviderExt(system: ExtendedActorSystem, config: Config, configPath: String) extends ReadJournalProvider {
  override def scaladslReadJournal: CassandraReadJournalScalaExt = new CassandraReadJournalScalaExt(config, configPath)(system)
  override def javadslReadJournal: CassandraReadJournalJavaExt = new CassandraReadJournalJavaExt(scaladslReadJournal)
}

private[lagom] class CassandraReadJournalJavaExt(journal: CassandraReadJournalScalaExt) extends akka.persistence.cassandra.query.javadsl.CassandraReadJournal(journal) {}

private[lagom] class CassandraReadJournalScalaExt(config: Config, configPath: String)(implicit system: ExtendedActorSystem) extends akka.persistence.cassandra.query.scaladsl.CassandraReadJournal(system, config) {
  private val slideMillsPath = "lagom.persistence.read-side.compaction.window-slide-mills"
  private val groupingSlideMills = if (config.hasPath(slideMillsPath)) config.getInt(slideMillsPath) else 1000

  override def eventsByTag(tag: String, offset: Offset): Source[EventEnvelope, NotUsed] = {
    super
      .eventsByTag(tag, offset)
      .groupBy(Integer.MAX_VALUE, _.event.getClass.getName)
      .groupedWithin(Integer.MAX_VALUE, groupingSlideMills millis)
      .filterNot(_.isEmpty)
      .map(orderedSeq => orderedSeq.last)
      .mergeSubstreamsWithParallelism(1)
  }
}
