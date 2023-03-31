rootProject.name = "cloud-event-store"
include("event-store")
include("event-store:aggregates")
findProject(":event-store:aggregates")?.name = "aggregates"
include("event-store:mongo-event-store")
findProject(":event-store:mongo-event-store")?.name = "mongo-event-store"
include("event-store:management")
findProject(":event-store:management")?.name = "management"
include("cloud-store-client")
include("cloud-store-service")
