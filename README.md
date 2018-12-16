#### Lagom Read Side extension
Provide an optimization for read side data synchronization. 
Can be used for cases when there is no need to react on each event on persistent actors, but can work with last by type in tag.
This can be useful when persistent actor represent something like counter that being changed frequently but 
there is no need to process each message, instead only last(by type in tag) for counter is enough to make a sync of data and save offset.
This can dramatically minimize number of redundant writes during read side data sync.


#### Development integration notes
1. Ensure correct settings in application.conf JdbcPersistentEntityRegistry 
```bash
# jdbc read side 
jdbc-read-journal.class = "com.lightbend.lagom.readside.JdbcReadJournalProviderExt"
# cassandra read side
jdbc-read-journal.class = "com.lightbend.lagom.readside.CassandraReadJournalProviderExt"
```
2. Ensure correct settings in application.conf 
```bash
lagom.persistence.read-side.compaction.window-slide-mills = 1000
```