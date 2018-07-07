package com.yemreak.modifiedsnake

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import java.io.IOException
import java.util.*

class SnakeEngine(private val context: Context, private val holder: SurfaceHolder) : SurfaceHolder.Callback, Runnable {

    /**
     * Ses modu çeşitleri
     * Not: Veriler değiştirilirse alttakilerin de değişmesi lazımdır.
     * @see R.layout.dialog_sounds Aynı sırada olmak zorundalar
     * @see setSoundsMode Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see getSoundsModeIndex Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see CustomDialogInfos.toggleButtonIds Aynı sırada ve sayıda olmak zorundalar.
     */
    enum class SoundsModes {
        NO_SOUNDS,
        CLASSIC_SOUNDS,
        RETRO_SOUNDS,
        GUN_SOUNDS,
        HORROR_SOUNDS
    }

    /**
     * Muzik modu çeşitleri
     * Not: Veriler değiştirilirse alttakilerin de değişmesi lazımdır.
     * @see R.layout.dialog_musics Aynı sırada olmak zorundalar
     * @see setMusicMode Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see getMusicModeIndex Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see CustomDialogInfos.toggleButtonIds Aynı sırada ve sayıda olmak zorundalar.
     */
    enum class MusicModes {
        NO_MUSICS,
        FERAMBIE,
        HORROR_MUSIC
    }

    /**
     * Control modu çeşitleri
     * Kullanıldığı Alan: Kontrol moduna göre farklı aktivite başlatılmaktadır.
     * @see MainActivity.onNewGameClicked
     *
     * Not: Veriler değiştirilirse alttakilerin de güncellenmesi lazımdır.
     * @see R.layout.dialog_controls Aynı sırada olmak zorundalar
     * @see setControlMode Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see getControlModeIndex Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see CustomDialogInfos.toggleButtonIds Aynı sırada ve sayıda olmak zorundalar.
     */
    enum class ControlModes {
        CONTROL_WITH_BUTTONS,
        CONTROL_WITH_SWIPE
    }

    /**
     * Tema modu çeşitleri
     * Not: Veriler değiştirilirse alttakilerin de değişmesi lazımdır.
     * @see Theme
     * @see R.layout.dialog_themes Aynı sırada olmak zorundalar
     * @see setThemeMode Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see getThemeModeIndex Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see CustomDialogInfos.toggleButtonIds Aynı sırada ve sayıda olmak zorundalar.
     */
    enum class ThemeModes {
        DEFAULT,
        DARKNESS,
        LIGHT,
        AQUA,
        PURPLE,
        HORROR_THEME
    }

    /**
     * Oyun modu çeşitleri
     * Not: Veriler değiştirilirse alttakilerin de değişmesi lazımdır.
     * @see R.layout.dialog_classic_modes Aynı sırada olmak zorundalar
     * @see setGameMode Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see getGameModeIndexes Aynı sırada ve aynı sayıda objeye sahip olmak zorundalar.
     * @see CustomDialogInfos.toggleButtonIds Aynı sırada ve sayıda olmak zorundalar.
     *
     * @sample FRAME Oyun dışındaki çerçeveyi kaldırır. Kenarlara çarpılmaz.
     * @see surfaceCreated
     * @see run
     *
     * @sample MORE_FEED_TYPES Birden fazla yem tipleri olur. 1, 2, 5, 10, 25 puanlık yemler.
     * @see Feed.spawnActTo
     *
     * @sample MORE_FEED Birden fazla yem olur.
     * @see initFeeds
     *
     * @sample SHRINK_SNAKE Yılan yem yedikçe büyümek yerine küçülür.
     * @see initVariables
     *
     */
    enum class GameModes {
        FRAME,
        MORE_FEED_TYPES,
        MORE_FEED,
        SHRINK_SNAKE
    }

    companion object {

        /**
         * Yılanın kontrol edilme modu
         * @see ControlModes
         */
        var controlMode = ControlModes.CONTROL_WITH_SWIPE
            private set(value) {
                field = value
            }

        /**
         * Oyun içindeki arka plan müziği modu
         * @see MusicModes
         */
        var musicMode = MusicModes.HORROR_MUSIC
            private set(value) {
                field = value
            }

        /**
         * Oyundaki ses efektleri modu
         * @see SoundsModes
         */
        var soundsMode = SoundsModes.HORROR_SOUNDS
            private set(value) {
                field = value
            }

        /**
         * Oyun modları
         * @see GameModes
         */
        var gameModes = arrayListOf<GameModes>()
            private set(value) {
                field = value
            }

        /**
         * Tema modu
         * @see ThemeModes
         */
        var themeMode = ThemeModes.DEFAULT
            private set(value) {
                field = value
            }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerini döndürür.
         * @return Seçilmiş ayarın buton indexi
         *
         * Not: İçeriği ControlModes'a bağlıdır.
         * @see ControlModes
         */
        fun getControlModeIndex(): Int {
            return when (controlMode) {
                ControlModes.CONTROL_WITH_BUTTONS -> 0
                ControlModes.CONTROL_WITH_SWIPE -> 1
            }
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerine uygun modu ayarlar.
         * @param mode Buton indexi
         *
         * Not: İçeriği ControlModes'a bağlıdır.
         * @see ControlModes
         */
        fun setControlMode(mode: Int) {
            controlMode = when (mode) {
                0 -> ControlModes.CONTROL_WITH_BUTTONS
                1 -> ControlModes.CONTROL_WITH_SWIPE
                else -> {
                    Log.e(
                            "Kontrol hatası",
                            "Kontrol ayarlanamadı. Hatalı mod değeri girildi. Lütfen sıralamanın aynı olmasına dikkat et. " +
                                    "\"SnakeEngine.ControlMode, CustomDialogActivity.setMode \" (SnakeEngine.setControlMode)"
                    )

                    ControlModes.CONTROL_WITH_SWIPE
                }
            }
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerini döndürür.
         * @return Seçilmiş ayarın buton indexi
         *
         * Not: İçeriği MusicModes'a bağlıdır.
         * @see MusicModes
         */
        fun getMusicModeIndex(): Int {
            return when (musicMode) {
                MusicModes.NO_MUSICS -> 0
                MusicModes.FERAMBIE -> 1
                MusicModes.HORROR_MUSIC -> 2
            }
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerine uygun modu ayarlar.
         * @param mode Buton indexi
         *
         * Not: İçeriği MusicModes'a bağlıdır.
         * @see MusicModes
         */
        fun setMusicMode(mode: Int) {
            musicMode = when (mode) {
                0 -> MusicModes.NO_MUSICS
                1 -> MusicModes.FERAMBIE
                2 -> MusicModes.HORROR_MUSIC
                else -> {
                    Log.e(
                            "Müzik ayarlama hatası",
                            "Müzik modu ayarlanamadı. Hatalı mod değeri girildi. Lütfen sıralamanın aynı olmasına dikkat et. " +
                                    "\"SnakeEngine.MusicModes, OptionActivity.showMusicsDialog\" (SnakeEngine.setMusicMode)"
                    )

                    MusicModes.NO_MUSICS
                }
            }
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerini döndürür.
         * @return Seçilmiş ayarın buton indexi
         *
         * Not: İçeriği SoundsModes'a bağlıdır.
         * @see SoundsModes
         */
        fun getSoundsModeIndex(): Int {
            return when (soundsMode) {
                SoundsModes.NO_SOUNDS -> 0
                SoundsModes.CLASSIC_SOUNDS -> 1
                SoundsModes.RETRO_SOUNDS -> 2
                SoundsModes.GUN_SOUNDS -> 3
                SoundsModes.HORROR_SOUNDS -> 4
            }
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerine uygun modu ayarlar.
         * @param mode Buton indexi
         *
         * Not: İçeriği SoundsModes'a bağlıdır.
         * @see SoundsModes
         */
        fun setSoundsMode(mode: Int) {
            soundsMode = when (mode) {
                0 -> SoundsModes.NO_SOUNDS
                1 -> SoundsModes.CLASSIC_SOUNDS
                2 -> SoundsModes.RETRO_SOUNDS
                3 -> SoundsModes.GUN_SOUNDS
                4 -> SoundsModes.HORROR_SOUNDS
                else -> {
                    Log.e(
                            "Ses ayarlama hatası",
                            "Ses modu ayarlanamadı. Hatalı mod değeri girildi. Lütfen sıralamanın aynı olmasına dikkat et. " +
                                    "\"SnakeEngine.SoundsModes, OptionActivity.showSoundsDialogs\" (SnakeEngine.setSoundsMode)"
                    )

                    SoundsModes.CLASSIC_SOUNDS
                }
            }
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerini döndürür.
         * @return Seçilmiş ayarın buton indexi
         *
         * Not: İçeriği GameModes'a bağlıdır.
         * @see GameModes
         */
        fun getGameModeIndexes(): ArrayList<Int> {
            val indexes = arrayListOf<Int>()

            if (gameModes.contains(GameModes.FRAME)) indexes.add(0)
            if (gameModes.contains(GameModes.MORE_FEED_TYPES)) indexes.add(1)
            if (gameModes.contains(GameModes.MORE_FEED)) indexes.add(2)
            if (gameModes.contains(GameModes.SHRINK_SNAKE)) indexes.add(3)

            return indexes
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerine uygun modu ayarlar.
         * @param mode Buton indexi
         *
         * Not: İçeriği GameModes'a bağlıdır.
         * @see GameModes
         */
        fun setGameMode(mode: Int) {
            when (mode) {
                0 -> changeGameMode(GameModes.FRAME)
                1 -> changeGameMode(GameModes.MORE_FEED_TYPES)
                2 -> changeGameMode(GameModes.MORE_FEED)
                3 -> changeGameMode(GameModes.SHRINK_SNAKE)
                else -> {
                    Log.e("Oyun Modu Ayarlanmadı", "Verilen index değeri tanımsız. Lütfen kontrol et; \" GameModeActivity.setMode \" . (SnakeEngine.setGameMode)")
                }
            }
        }

        /**
         * Oyun modunu değiştirme
         * Not: Aynı modun birden fazla eklenmesini önler.
         */
        private fun changeGameMode(mode: GameModes) {
            if (gameModes.contains(mode))
                gameModes.remove(mode)
            else
                gameModes.add(mode)
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerini döndürür.
         * @return Seçilmiş ayarın buton indexi
         *
         * Not: İçeriği ThemeModes'a bağlıdır.
         * @see ThemeModes
         */
        fun getThemeModeIndex(): Int {
            return when (themeMode) {
                ThemeModes.DEFAULT -> 0
                ThemeModes.DARKNESS -> 1
                ThemeModes.LIGHT -> 2
                ThemeModes.AQUA -> 3
                ThemeModes.PURPLE -> 4
                ThemeModes.HORROR_THEME -> 5
            }
        }

        /**
         * Ayarlar diyalog menüsündeki buton indexlerine uygun modu ayarlar.
         * @param mode Buton indexi
         *
         * Not: İçeriği ThemeModes'a bağlıdır.
         * @see ThemeModes
         */
        fun setThemeMode(mode: Int) {
            themeMode = when (mode) {
                0 -> ThemeModes.DEFAULT
                1 -> ThemeModes.DARKNESS
                2 -> ThemeModes.LIGHT
                3 -> ThemeModes.AQUA
                4 -> ThemeModes.PURPLE
                5 -> ThemeModes.HORROR_THEME
                else -> {
                    Log.e(
                            "Tema hatası",
                            "Tema ayarlanamadı. Hatalı mod değeri girildi. Lütfen sıralamanın aynı olmasına dikkat et. " +
                                    "\"SnakeEngine.ThemeModes, OptionActivity.showThemeDialog\" (SnakeEngine.setThemeMode)"
                    )

                    ThemeModes.DEFAULT
                }
            }
        }
    }

    /**
     * Oyunun teması
     */
    val theme = Theme.getTheme(context, themeMode)

    /**
     * Arka plan fon müziği
     * @see initSounds
     */
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Ses efekti değişkenleri
     * @see initSounds
     */
    private lateinit var soundPool: SoundPool
    /**
     * Yem yeme sesi
     * @see initSounds
     */
    private var soundEat: Int = -1
    /**
     * Çarpma sesi
     * @see initSounds
     */
    private var soundCrash: Int = -1


    /**
     * Oyun yüzeyine bağlı işlemlerin hatasız çalışması için değişken
     * @see surfaceCreated
     */
    var isSurfaceCreated = false
        private set(value) {
            field = value
        }

    /**
     * Oyun motorunun durumuna bağlı işlemlerin hatasız çalışması için değişken
     * @see surfaceCreated
     */
    var isPaused = false
        private set(value) {
            field = value
        }
    /**
     * Oyun motorunun durumuna bağlı işlemlerin hatasız çalışması için değişken
     * @see prepare
     */
    var isStarted = false
        private set(value) {
            field = value
        }
    /**
     * Değişkenlerin tanımlanmasına bağlı işlemlerin hatasız çalışması için değişken
     */
    var areVariablesDefined = false
        private set(value) {
            field = value
        }

    /**
     * Oyunun çerçevesinin olunabilirliği
     */
    var hasFrame = gameModes.contains(GameModes.FRAME)
        private set(value) {
            field = value
        }

    /**
     * Oyun ekranındaki yılan
     */
    var snake: Snake = Snake()
        private set(value) {
            field = value
        }

    /**
     * Yılanın varsayılan başlangıç uzunluğu
     */
    var defaultLengthOfSnake: Int = 4

    /**
     * Oyun ekranındaki yemler
     */
    val feeds = ArrayList<Feed>()

    /**
     * Oyundaki skor
     */
    var score: Int = 0
        private set(value) {
            field = value
        }

    /**
     * Oyun motoru yenileyicisi
     * Not: Değişken olarak tanımlanma sebebi, durdurma gereksinimidir.
     * @see pause
     */
    var handler = Handler()

    /**
     * Saniyelik yenilenme sayısı
     */
    var fps: Int = 10

    init {
        initSounds()
    }

    /**
     * Ses ve müzik değişkenlerinin tanımlanması ve oluşturulması.
     */
    private fun initSounds() {
        mediaPlayer = when (musicMode) {
            MusicModes.NO_MUSICS -> null
            MusicModes.FERAMBIE -> MediaPlayer.create(context, R.raw.bg_music1)
            MusicModes.HORROR_MUSIC -> MediaPlayer.create(context, R.raw.bg_horror)
        }

        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)

        try {
            val mode = when (soundsMode) {
                SoundsModes.NO_SOUNDS -> return // Bu kısmı incele "return" sounds değişkenleri atlanmalı mı?
                SoundsModes.CLASSIC_SOUNDS -> "classic"
                SoundsModes.RETRO_SOUNDS -> "retro"
                SoundsModes.GUN_SOUNDS -> "gun"
                SoundsModes.HORROR_SOUNDS -> "horror"
            }

            var descriptor = context.assets.openFd("Sounds/${mode}_eating.wav")
            soundEat = soundPool.load(descriptor, 0)

            descriptor = context.assets.openFd("Sounds/${mode}_crash.wav")
            soundCrash = soundPool.load(descriptor, 0)


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Oyun alanı çizildiğinde yapılacak oyun motoru işlemleri
     */
    override fun surfaceCreated(holder: SurfaceHolder) {
        // Yüzey kontrolu için
        isSurfaceCreated = true

        if (isPaused) {
            // Ekrana çizme
            draw()
        } else {
            BlockField.initVariables(holder.surfaceFrame, hasFrame)
            prepare()
        }
    }

    /**
     * Oyun alanı değiştiğinde yapılacak oyun motoru işlemleri
     */
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    /**
     * Oyun alanı silindiğinde yapılacak oyun motoru işlemleri
     */
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        isSurfaceCreated = false
    }

    /**
     * Oyunu hazır hale getirip, durgun halde bekletme
     * Not: Motor çalışırken kullanılamaz.
     * @see start
     * Not: Yüzey oluşturulmadan çalışmaz.
     * @see surfaceCreated
     */
    private fun prepare() {
        if (!isStarted && isSurfaceCreated) {
            initVariables()
            newGame()
            isPaused = true
        } else {
            Log.e(
                    "Fonksiyon Çalışmadı",
                    "Zaten çalışan motoru hazır hale getiremeyiz. Yüzey alanı oluşturulmadan çalıştırılamaz, genelde SurfaceCreated içerisinden çağırılır." +
                            "\"surfaceCreated\" metodunu kontrol et. (SnakeEngine.prepare)"
            )
        }
    }

    /**
     * Değişkenlerin tanımlanması
     * Not: Yüzey oluşturulmadan çalışmaz.
     * Not: Oyun moduna göre şekillenir.
     * @see surfaceCreated
     */
    private fun initVariables() {
        if (isSurfaceCreated) {

            // Oyun moduna göre farklı tanımalama şekilleri olacak
            snake = when {
                gameModes.contains(GameModes.SHRINK_SNAKE) -> Snake(Snake.maxLength)
                else -> Snake(defaultLengthOfSnake)
            }

            initFeeds()

            areVariablesDefined = true

        } else {
            Log.e(
                    "Fonksiyon Çalışmadı",
                    "Yüzey alanı ve field oluşturulmadan çalıştırılamaz, genelde SurfaceCreated'de  çağırılır." +
                            "\"prepare, surfaceCreated\" metodunu kontrol et. (SnakeEngine.initVariables)"
            )
        }
    }

    /**
     * Not: Sadece bir kere kullanılmalı
     */
    private fun initFeeds() {
        when { // Yemleri oluşturma
            gameModes.contains(GameModes.MORE_FEED) -> {
                for (i in 0..Random().nextInt(8)) // Düzenle
                    feeds.add(Feed())
            }
            else -> {
                feeds.add(Feed())
            }
        }
    }

    /**
     * Yeni oyunun oluşturup, çizilmesi
     * Not: Değişkenler tanımlanmadan çalışmaz.
     * Not: Oyun moduna göre şekillenir.
     * @see initVariables
     */
    private fun newGame() {
        if (areVariablesDefined) {
            snake.create()

            spawnFeeds()

            score = 0

            // Oluşturulan oyunu ekrana çizmek için (Kaldırılabilir)
            draw()

        } else {
            Log.e(
                    "Fonksiyon Çalışmadı",
                    "Değişkenler tanımlanmadan çalıştırılamaz." +
                            "Metodu kullanmadan önce \"initVariables\" metodunu kullandığından emin ol. (SnakeEngine.newGame)"
            )
        }
    }

    /**
     * Tüm yemlerin ekrana yerleştirilmesi
     */
    private fun spawnFeeds() {
        feeds.forEach { feed -> feed.spawnActTo(snake, feeds, gameModes) }
    }

    /**
     * Verilerin yüzeye çizdirilmesi
     * Not: Değişkenler tanımlanmadan çalışmaz.
     * @see initVariables
     */
    private fun draw() {
        if (isSurfaceCreated && areVariablesDefined) {
            if (holder.surface.isValid) {
                val canvas: Canvas = holder.lockCanvas()
                val paint = Paint()

                // Kenar boşluklarını çizme
                paint.color = Color.BLACK
                canvas.drawPaint(paint)

                // Eğer varsa çerçeveyi çizme
                if (hasFrame) {
                    paint.color = theme.colorBg
                    canvas.drawRect(BlockField.frame, paint)
                }

                // Oyun ekranını çiziyoruz
                paint.color = theme.colorField
                canvas.drawRect(BlockField.field, paint)

                // Score'u çiziyoruz
                paint.color = theme.colorScore
                paint.textSize = 64f
                canvas.drawText("Score: $score", 30f, 94f, paint)

                /*Dışa doğru çizilmeli içe dğeil
                paint.color = context.resources.getColor(R.color.ultra_violet)
                canvas.drawRect(theBlockFrame(Snake.Directions.LEFT), paint)
                canvas.drawRect(theBlockFrame(Snake.Directions.RIGHT), paint)
                canvas.drawRect(theBlockFrame(Snake.Directions.DOWN), paint)
                canvas.drawRect(theBlockFrame(Snake.Directions.UP), paint)
                */

                // Yılanı çiziyoruz
                paint.color = theme.colorSnake
                snake.drawHead(canvas, paint)
                snake.drawTails(canvas, paint)

                // Yılanın gözünü çizme
                paint.color = theme.colorSnakeEyes
                snake.drawEyes(canvas, paint)

                // Yemi çiziyoruz
                paint.color = theme.colorFeed
                feeds.forEach { feed -> feed.draw(context, canvas, paint) }

                holder.unlockCanvasAndPost(canvas)
            }
        } else {
            Log.e(
                    "Fonksiyon Çalışmadı",
                    "Değişkenler tanımlanmadan çalıştırılamaz." +
                            "Metodu kullanmadan önce \"initVariables\" metodunu kullandığından emin ol. (SnakeEngine.drawTails)"
            )
        }

    }

    /**
     * YılanMotor'unun çalışması
     * Not: Motor çalıştırılmadan çalışmaz.
     * @see start
     * Not: Motor durdulduysa çalışmaz.
     * @see pause
     * Not: Yüzey oluşturulmadan çalışmaz.
     * @see surfaceCreated
     * Not: Değişkenler tanımlanmadan çalışmaz.
     * @see initVariables
     */
    override fun run() {
        if (isStarted && !isPaused && isSurfaceCreated && areVariablesDefined) {
            snake.move(hasFrame)


            feeds.forEach { feed ->
                if (snake.isFeeding(feed.center)) {
                    eatFeed(feed)
                }
            }

            if (snake.isCrashed(hasFrame)) {
                soundPool.play(soundCrash, 1f, 1f, 0, 0, 1f)
                newGame()
            }

            draw()

            handler.postDelayed(this, (1000 / fps).toLong())
        } else {
            Log.e(
                    "Fonksiyon Çalışmadı",
                    "Motor başlatılmadan, yüzey oluşturulmadan, değişkenler tanımlanmadan çalıştırılamaz." +
                            "Metodu kullanmadan önce \"start, pause, surfaceCreated, initVariables\" metodlarını kullandığından emin ol. (SnakeEngine.run)"
            )
        }
    }

    /**
     * Yem yenildiğinde olacak işlemler
     * Not: Değişkenler tanımlanmadan çalışmaz.
     * Not: Oyun moduna göre şekillenir.
     * @see initVariables
     */
    private fun eatFeed(feed: Feed) {
        if (areVariablesDefined) {
            incScore(feed.feedType)

            soundPool.play(soundEat, 1f, 1f, 0, 0, 1f)

            snake.eatFeedActTo(gameModes, feed.feedType)

            spawnFeeds()

        } else {
            Log.e(
                    "Fonksiyon Çalışmadı",
                    "Değişkenler tanımlanmadan çalıştırılamaz." +
                            "Metodu kullanmadan önce \"initVariables\" metodunun kullandığından emin ol. (SnakeEngine.eatFeed)"
            )
        }
    }

    /**
     * Yem tipine göre skor arttırma
     */
    private fun incScore(feedType: Feed.FeedTypes) {
        score += when (feedType) {
            Feed.FeedTypes.NORMAL -> 1
            Feed.FeedTypes.DOUBLE_FEED -> 2
            Feed.FeedTypes.X5_FEED -> 5
            Feed.FeedTypes.X10_FEED -> 10
            Feed.FeedTypes.X25_FEED -> 25
        }
    }

    /**
     * Oyun motorunu durumuna göre başlatır veya durdurur.
     */
    fun pauseOrStart() {
        when (isPaused) {
            true -> startSensible()
            else -> pause()
        }
    }

    /**
     * Motorun "kontrollü" başlatılması veya devam ettirilmesi
     * Not: Yüzey oluşturulmadan çalışmaz.
     * @see surfaceCreated
     */
    fun startSensible() {
        if (!isStarted || isPaused) {
            start()
        }
    }

    /**
     * Motorun başlatılması veya devam ettirilmesi
     * Not: Yüzey oluşturulmadan çalışmaz.
     * @see surfaceCreated
     */
    private fun start() {
        if (isSurfaceCreated) {
            if (mediaPlayer != null) {
                mediaPlayer!!.setVolume(0.7f, 0.7f)
                mediaPlayer!!.isLooping = true
                mediaPlayer!!.start()
            }


            isStarted = true
            isPaused = false

            handler.postDelayed(this, (1000 / fps).toLong())
        } else {
            Log.e(
                    "Fonksiyon Çalışmadı",
                    "Yüzey oluşturulmadan çalıştırılamaz." +
                            "Metodu kullanmadan önce \"surfaceCreated\" metodunun kullandığından emin ol. (SnakeEngine.start)"
            )
        }
    }

    /**
     * Oyun motorunun durdurulması.
     */
    fun pause() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying)
                mediaPlayer!!.pause()
        }

        handler.removeCallbacks(this)

        isPaused = true

    }
}



























