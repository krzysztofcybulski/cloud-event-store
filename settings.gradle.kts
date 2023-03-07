rootProject.name = "cloud-event-store"
include("event-store")
include("event-store:aggregates")
findProject(":event-store:aggregates")?.name = "aggregates"
include("cloud-store")
include("event-store:mongo-event-store")
findProject(":event-store:mongo-event-store")?.name = "mongo-event-store"
