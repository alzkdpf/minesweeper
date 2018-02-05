package com.example.michaellim.minegame

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import hugo.weaving.DebugLog
import org.jetbrains.anko.*
import rx.subscriptions.CompositeSubscription


class MainActivity : AppCompatActivity() {

    val TAG = "test";
    val DEBUG_FLAG = false;

    //subscription
    private var subscriptions = CompositeSubscription()

    //button select listener
    var switchBtnListener = { v: View? ->
        when(v?.id)
        {
            R.id.cbOpen ->
                clickBtnMode = SWITCH_BUTTON.OPEN

            R.id.cbCheck ->
                clickBtnMode = SWITCH_BUTTON.CHECK
        }
    }

    /**
     * configuration
     */
    val ROW = 10
    val COL = 10
    val MINE_COUNT = 10

    //theme
    val DEFAULT_BOX_STYLE = R.drawable.btn_type_border

    //box open count
    var openCount = 1;
    //flag count
    var checkFlagCount = 10

    //view
    val BOX_VIEW = Array<Array<Button?>>(ROW, {arrayOfNulls<Button>(COL)})
    //mine
    val MINE = Array<IntArray>(ROW, { IntArray(COL) })
    //box status
    val BOX_STATUS = Array<Array<CHECK_STATUS?>>(ROW, { arrayOfNulls<CHECK_STATUS>(COL)})

    //default button click mode
    var clickBtnMode : SWITCH_BUTTON = SWITCH_BUTTON.OPEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //game init
        initGame()

        //create game view
        createView()

    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
    }

    /**
     * create game view
     */
    fun createView() {
        val widthVal = Resources.getSystem().displayMetrics.widthPixels / COL;

        verticalLayout {
            for (row in 0..ROW - 1) {
                linearLayout {
                    linearLayout {
                        for (col in 0..COL - 1) {
                            button()
                            {
                                lparams(width = widthVal, height = widthVal)
                                BOX_VIEW[row][col] = this

                                onClick {
                                    view ->
                                    when(clickBtnMode)
                                    {
                                        SWITCH_BUTTON.OPEN -> openBox(row, col)
                                        else -> checkFlag(view,row,col)
                                    }
                                }
                                if (MINE[row][col] == MINE_STATUS.MINE.value && DEBUG_FLAG == true) {
                                    backgroundColor = Color.BLUE
                                }else{
                                    backgroundResource = DEFAULT_BOX_STYLE
                                }
                            }
                        }
                    }.orientation = LinearLayout.HORIZONTAL
                }.orientation = LinearLayout.VERTICAL
            }//for
            linearLayout {
                radioGroup {
                    radioButton {
                        id = R.id.cbOpen
                        text = "Open~"
                        onClick(switchBtnListener)
                    }.isChecked = true
                    radioButton {
                        id = R.id.cbCheck
                        text = "MINE Check"
                        onClick(switchBtnListener)
                    }

                }
            }.orientation = LinearLayout.VERTICAL
        }
    }

    /**
     * check MINE
     */
    fun checkFlag(view : View?, row : Int , col : Int)
    {
        //pass already open box
        if(BOX_STATUS[row][col] == CHECK_STATUS.OPEN)
        {
            return
        }

        if(BOX_STATUS[row][col] != CHECK_STATUS.FLAG)
        {
            //할당된 깃발 모두 사용시
            if(checkFlagCount == 0)
            {
                toast("사용 가능한 지뢰 체크 갯수를 초과했습니다.")
                return
            }

            view?.setBackgroundColor(Color.RED)
            BOX_STATUS[row][col] = CHECK_STATUS.FLAG
            checkFlagCount--;
        }else{
            view?.setBackgroundResource(DEFAULT_BOX_STYLE)
            BOX_STATUS[row][col] = CHECK_STATUS.CLOSE
            checkFlagCount++;
        }
    }

    /**
     * init game configuration
     */
    fun initGame() {
        //flag count init
        checkFlagCount = 10;

        //init
        for (row in 0 until ROW - 1) {
            for (col in 0 until COL - 1) {
                MINE[row][col] = MINE_STATUS.NOT_SET.value
                BOX_STATUS[row][col] = CHECK_STATUS.CLOSE
            }
        }

        //create mine
        var check = 1

        do {
            var x = (0 until ROW - 1).random()
            var y = (0 until COL - 1).random()

            if (MINE[x][y] != MINE_STATUS.MINE.value) {
                MINE[x][y] = MINE_STATUS.MINE.value
                println("${x},${y}")
                println("check ${check}")
                check++
            }

        } while (MINE_COUNT >= check)

        setMineCount()
    }

    /**
     * Set Mine Count
     */
    fun setMineCount() {
        for (x in 0 until ROW - 1) {
            for (y in 0 until COL - 1) {
                var check_MINE = 0

                if (MINE[x][y] != MINE_STATUS.MINE.value) {
                    if (isExistMine(x - 1, y - 1)) check_MINE++;
                    if (isExistMine(x - 1, y)) check_MINE++;
                    if (isExistMine(x - 1, y + 1)) check_MINE++;
                    if (isExistMine(x, y - 1)) check_MINE++;
                    if (isExistMine(x, y + 1)) check_MINE++;
                    if (isExistMine(x + 1, y - 1)) check_MINE++;
                    if (isExistMine(x + 1, y)) check_MINE++;
                    if (isExistMine(x + 1, y + 1)) check_MINE++;

                    MINE[x][y] = check_MINE
                }
            }
        }
    }

    /**
     * is mine?
     */
    fun isExistMine(x: Int, y: Int): Boolean {
        if (x < 0 || y < 0 || x >= ROW || y >= COL) {
            return false
        }
        return MINE[x][y] == MINE_STATUS.MINE.value
    }

    /**
     * left click
     */
    fun openBox(x: Int, y: Int) {
        if (x < 0 || y < 0 || x >= ROW || y >= COL) {
            return
        }

        if (gameIsEnd()) {
            gameResult()
        }

        if (BOX_STATUS[x][y] == CHECK_STATUS.OPEN) {
            return
        }

        if (MINE[x][y] > 0) {
            boxUpdate(x,y)
            return
        }

        if (isExistMine(x, y)) {
            showDlg("지뢰 폭발! -End-",
                    {finish()})
            return
        }

        if (MINE[x][y] == 0) {
            boxUpdate(x,y)
            openArroundBox(x,y)
        }

    }

    /**
     * box view update
     */
    fun boxUpdate(row: Int, col: Int)
    {
        openCount++;
        BOX_STATUS[row][col] = CHECK_STATUS.OPEN
        BOX_VIEW[row][col]?.setBackgroundResource(R.drawable.btn_type_border_open);
        BOX_VIEW[row][col]?.text = MINE[row][col].toString()
    }


    /**
     * arround box open
     */
    @DebugLog
    fun openArroundBox(x: Int, y: Int) {
        openBox(x - 1, y - 1);
        openBox(x, y - 1);
        openBox(x + 1, y - 1);
        openBox(x - 1, y);
        openBox(x + 1, y);
        openBox(x - 1, y + 1);
        openBox(x, y + 1);
        openBox(x + 1, y + 1);
    }

    /**
     * game over check
     */
    fun gameIsEnd() : Boolean
    {
        val total = ROW * COL
        Log.d(TAG,"count : ${openCount}")
        if( (total - MINE_COUNT) <= openCount)
        {
            return  true;
        }

        return false;
    }

    /**
     * game end
     */
    fun gameResult()
    {
        var findMINE = 0;
        for (row in 0 until ROW - 1) {
            for (col in 0 until COL - 1) {
                if(MINE[row][col] == MINE_STATUS.MINE.value && BOX_STATUS[row][col] == CHECK_STATUS.FLAG)
                {
                    findMINE ++;
                }
            }
        }

        showDlg("찾은 폭탄 갯수 : ${findMINE} -End-",
                {finish()})
    }

    fun showDlg(message : String, positive : () -> Unit)
    {
        alert(message, resources.getString(R.string.app_name))
        {
            positiveButton { positive() }
        }.show()
    }
}
