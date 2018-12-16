package readside
import akka.NotUsed
import akka.actor.ExtendedActorSystem
import akka.persistence.jdbc.query.scaladsl
import akka.persistence.query.{EventEnvelope, ReadJournalProvider}
import akka.stream.scaladsl.Source
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.language.postfixOps

class JdbcReadJournalProviderExt(system: ExtendedActorSystem, config: Config, configPath: String) extends ReadJournalProvider {
  override def scaladslReadJournal: JdbcReadJournalScalaExt = new JdbcReadJournalScalaExt(config, configPath)(system)
  override def javadslReadJournal: JdbcReadJournalJavaExt = new JdbcReadJournalJavaExt(scaladslReadJournal)
}

private[lagom] class JdbcReadJournalJavaExt(journal: JdbcReadJournalScalaExt) extends akka.persistence.jdbc.query.javadsl.JdbcReadJournal(journal) {}

private[lagom] class JdbcReadJournalScalaExt(config: Config, configPath: String)(implicit system: ExtendedActorSystem) extends scaladsl.JdbcReadJournal(config, configPath)(system) {
  private val slideMillsPath = "lagom.persistence.read-side.compaction.window-slide-mills"
  private val groupingSlideMills = if (config.hasPath(slideMillsPath)) config.getInt(slideMillsPath) else 1000

  override def eventsByTag(tag: String, offset: Long): Source[EventEnvelope, NotUsed] = {
    super
      .eventsByTag(tag, offset)
      .groupBy(Integer.MAX_VALUE, _.event.getClass.getName)
      .groupedWithin(Integer.MAX_VALUE, groupingSlideMills millis)
      .filterNot(_.isEmpty)
      .map(orderedSeq => orderedSeq.last)
      .mergeSubstreamsWithParallelism(1)
  }
}
