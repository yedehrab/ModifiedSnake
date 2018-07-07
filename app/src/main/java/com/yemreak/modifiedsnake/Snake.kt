package com.yemreak.modifiedsnake

import android.graphics.*
import android.util.Log
import com.yemreak.modifiedsnake.Snake.Directions.*

class Snake() {
    /**
     * Yılanın başının merkez noktaları
     */
    var centerHead: Point = Point()
        private set(value) {
            field = value
        }

    /**
     * Yılanın kuyruğunun merkez noktaları
     */
    val centerTails = ArrayList<Point>()

    /**
     * Yılanın varsayılan uzunluğu
     * Not: Modsuz haldeki uzunluktur.
     */
    var defaultLength = 4
        private set(value) {
            field = value
        }

    /**
     * Yön belirteci classı
     */
    enum class Directions {
        LEFT, UP, RIGHT, DOWN
    }

    companion object {

        /**
         * Başlangıç yönü
         */
        var direction = RIGHT
            private set(value) {
                field = value
            }

        /**
         * Yeni başlangıç yönü
         * Hatırlatma: Yılanın yönü bir adımda birden fazla değiştirilmesin diye yapıldı. Sağ'a giden bir yılana *yukarı - sol* verileri verince (yukarı dönmeden sol verisi veriliyor) ölmüş sayılıyordu engellendi.
         * @see SnakeActivity1.onButtonClick
         * @see SnakeActivity2.svFullGameFieldOnTouch
         */
        var newDirection = RIGHT

        /**
         * Shrink game modundaki yılanın başlangıç uzunluğu
         * @see SnakeEngine.GameModes
         */
        var maxLength = 100
    }

    constructor(length: Int) : this() {
        defaultLength = length
    }

    /**
     * Yılanı oyun alanında oluşturmak
     */
    fun create() {
        // Yönü sıfırlıyoruz
        direction = RIGHT

        // Kuyruğu temizliyoruz
        centerTails.clear()

        // Başı merkezde oluşturuyoruz. *Kopyasını* atıyoruz.
        centerHead = Point(BlockField.centerPoint())

        // Her bir kuyruğu oluşturuyoruz.
        for (i in 1 until defaultLength) {
            centerTails.add(BlockField.point(centerHead, -i, 0))
        }
    }

    /**
     * Yılanın hareket etmesi
     */
    fun move(hasFrame: Boolean) {
        for (i in centerTails.lastIndex downTo 0) {
            if (i == 0) {
                centerTails[i] = Point(centerHead) // Not: Direk atama işlemi yaparsak adresi kopyalanır.
                continue
            }
            centerTails[i] = Point(centerTails[i - 1]) // Not: Direk atama işlemi yaparsak adresi kopyalanır.
        }

        refreshDireciton() // Yeni hareket için yönümüzü güncelliyoruz
        centerHead = BlockField.besidePoint(centerHead, hasFrame, direction)
    }

    /**
     * Yılanın yönünü yeni yön istediğine göre günceller.
     * @see newDirection
     */
    private fun refreshDireciton() {
        when (direction) {
            LEFT ->
                if (newDirection != RIGHT)
                    direction = newDirection
            UP ->
                if (newDirection != DOWN)
                    direction = newDirection
            RIGHT ->
                if (newDirection != LEFT)
                    direction = newDirection
            DOWN ->
                if (newDirection != UP)
                    direction = newDirection
        }
    }

    /**
     * Yılanın yem yemesi
     */
    fun isFeeding(feed: Point): Boolean {
        return feed.equals(centerHead.x, centerHead.y)
    }

    /**
     * Oyun moduna göre yılanın yem yeme durumunda yapılan işlemleri yapar.
     * @param gameModes Oyun Modları Listesi
     * @param feedType Yinelen yemin tipi
     */
    fun eatFeedActTo(gameModes: ArrayList<SnakeEngine.GameModes>, feedType: Feed.FeedTypes) {
        // Oyun moduna göre değişecek kısım
        if (gameModes.contains(SnakeEngine.GameModes.SHRINK_SNAKE)) {
            when (feedType) {
                Feed.FeedTypes.NORMAL -> grownDown()
                Feed.FeedTypes.DOUBLE_FEED -> grownDown(2)
                Feed.FeedTypes.X5_FEED -> grownDown(5)
                Feed.FeedTypes.X10_FEED -> grownDown(10)
                Feed.FeedTypes.X25_FEED -> grownDown(25)
            }
        } else {
            when (feedType) {
                Feed.FeedTypes.NORMAL -> growUp()
                Feed.FeedTypes.DOUBLE_FEED -> growUp(2)
                Feed.FeedTypes.X5_FEED -> growUp(5)
                Feed.FeedTypes.X10_FEED -> growUp(10)
                Feed.FeedTypes.X25_FEED -> growUp(25)
            }
        }
    }

    /**
     * Yılanın birden fazla büyümesi
     */
    private fun growUp(num: Int) {
        for (i in 0 until num)
            growUp()
    }

    /**
     * Yılanın büyümesi
     */
    private fun growUp() {
        when (centerTails.size) {
            0 -> centerTails.add(BlockField.behindPoint(centerHead, direction))
            else -> centerTails.add(centerTails.last())
        }
    }

    /**
     * Yılanın birden fazla küçülmesi
     */
    private fun grownDown(num: Int) {
        for (i in 0 until num)
            grownDown()
    }

    /**
     * Yılanın küçülmesi
     */
    private fun grownDown() {
        when (centerTails.size) {
            0 -> return
            else -> centerTails.remove(centerTails.last())
        }
    }

    /**
     * Yılanın kuyruğunun ekrana çizilmesi
     */
    fun drawTails(canvas: Canvas, paint: Paint) {
        centerTails.forEach { tail ->
            canvas.drawRect(tail, paint)
        }
    }

    /**
     * Merkez noktalarına göre ekrana dörtgen çizme
     */
    private fun Canvas.drawRect(center: Point, paint: Paint) {
        this.drawRect(BlockField.getBlock(center), paint)
    }

    /**
     * Yılanın başının ekrana çizilmesi
     */
    fun drawHead(canvas: Canvas, paint: Paint) {
        canvas.drawRect(centerHead, paint)
        Log.e("HEAD", "Head: ${BlockField.getBlock(centerHead)}, GapY ${BlockField.gap.y}")
    }

    /**
     * Yılanın gözlerinin *yöne uygun şekilde* ekrana çizilmesi
     */
    fun drawEyes(canvas: Canvas, paint: Paint) {
        val angle: Float = when (direction) {
            LEFT -> 180f
            UP -> 270f
            RIGHT -> 0f
            DOWN -> 90f
        }

        canvas.save()
        canvas.rotate(angle, BlockField.getBlock(centerHead).exactCenterX(), BlockField.getBlock(centerHead).exactCenterY())

        val leftEyes = RectF(
                BlockField.getBlock(centerHead).right - 3 * BlockField.blockSize / 7f,
                BlockField.getBlock(centerHead).top + BlockField.blockSize / 7f,
                BlockField.getBlock(centerHead).right - BlockField.blockSize / 7f,
                BlockField.getBlock(centerHead).top + 3 * BlockField.blockSize / 7f
        )

        val rightEyes = RectF(
                BlockField.getBlock(centerHead).right - 3 * BlockField.blockSize / 7f,
                BlockField.getBlock(centerHead).bottom - 3 * BlockField.blockSize / 7f,
                BlockField.getBlock(centerHead).right - BlockField.blockSize / 7f,
                BlockField.getBlock(centerHead).bottom - BlockField.blockSize / 7f
        )

        canvas.drawRect(leftEyes, paint)
        canvas.drawRect(rightEyes, paint)

        canvas.restore()
    }

    /**
     * Çerçeve durumuna göre yılanın çarpışma durumunu bulma
     * @param hasFrame Çerçeve varsa true, yoksa false
     * @return Çarpışma varsa true, yoksa false
     */
    fun isCrashed(hasFrame: Boolean): Boolean {
        return when (hasFrame) {
            true -> when {
                centerHead.x < 0 -> true
                centerHead.x >= BlockField.blockNum.x -> true
                centerHead.y < 0 -> true
                centerHead.y >= BlockField.blockNum.y -> true
                isTail() -> true
                else -> false
            }
            false -> when {
                isTail() -> true
                else -> false
            }

        }
    }

    /**
     * Yılan kuyruğuna mı deyiyor kontrolü
     */
    private fun isTail(): Boolean {
        return centerTails.contains(centerHead)
    }


}