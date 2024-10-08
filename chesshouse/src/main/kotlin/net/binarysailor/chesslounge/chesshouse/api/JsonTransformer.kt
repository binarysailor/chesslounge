package net.binarysailor.chesslounge.chesshouse.api

import com.google.gson.Gson
import spark.ResponseTransformer

internal class JsonTransformer: ResponseTransformer {
    private val gson = Gson()
    override fun render(model: Any?): String {
        return gson.toJson(model)
    }
}