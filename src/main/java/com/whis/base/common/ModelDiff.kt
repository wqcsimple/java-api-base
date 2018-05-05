package com.whis.base.common

import com.fasterxml.jackson.annotation.JsonProperty

class ModelDiff(@field:JsonProperty("old") var old: Any?,
                @field:JsonProperty("new") var new: Any?)
