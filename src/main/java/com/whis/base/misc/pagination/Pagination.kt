package com.whis.base.misc.pagination

class Pagination {
    
    data class PageConfig(val offset: Int, val length: Int)

    companion object {

        @JvmStatic
        fun config(page: Int): PageConfig {
            return config(page, 10)
        }

        @JvmStatic
        fun config(page: Int, length: Int): PageConfig {
            val p = if (page < 1) 1 else page
            val offset = (p - 1) * length

            return PageConfig(offset, length)
        }
    }
}