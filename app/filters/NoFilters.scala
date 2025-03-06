package filters

import play.api.http.HttpFilters

// This filter chain returns an empty sequence of filters.
class NoFilters extends HttpFilters {
  override val filters = Seq.empty
}

