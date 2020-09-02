package receiving.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import receiving.service.ReceivingService

class ReceivingRoute {
  private val receivingService = new ReceivingService

  def receivingRoute: Route = pathPrefix("receiving") {
    post {
      extractRequestContext { ctx =>
        import ctx.materializer
        onSuccess(receivingService.process(ctx.request)) { result =>
          import de.heikoseeberger.akkahttpjackson.JacksonSupport._
          complete(result)
        }
      }
    }
  }
}
