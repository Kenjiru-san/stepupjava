package fxabc2;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

// このクラスの中でwkm:walkingManとmanHoleの初期化と描画を実施する
/*
 * 上段、下段があり、上段は以下のとおり。
 * 表示されるのは、1～12の位置
 * 0番から11番の12個の箱（表示しない）があり、上段では順に左から右に箱が流れ、位置が
 * 移動するが、位置12の次は位置1に戻る。
 * 箱には人が入っている場合と入っていない場合があり、箱を位置1に
 * セットした後に、人が入るかどうかを乱数で50%の確率で決める。
 * ただし、マンホールの穴が５つ間隔で開いているので、５つ先の箱に人が入っている場合には
 * 人が入らない。
 * 下段の0番から11番の12個の箱は、順に右から左に流れ、位置が移動していくが、位置1の次は
 * 位置12に戻る。 他は上段と同じ。
 * 上段→下段→上段→下段→・・・のように上段の箱が移動した後に下段の箱が移動し、上段、下段は
 * 同時には移動しない。
 */

public class iterateN extends MainAppli{

	static final int WIDTH 	= 640;
	static final int HEIGHT = 480;
	static final int FPS 	= 1; //Frame per Second,1秒当たりのフレーム数
	static final int numOfBox 	= 12;//箱の数
	static final int numOfHole 	= 4;//マンホールの穴の数
	static final byte LU = 1;//マンホールのフタの位置Left Upper
	static final byte RU = 2;//マンホールのフタの位置Right Upper
	static final byte LL = 3;//マンホールのフタの位置Left Lower
	static final byte RL = 4;//マンホールのフタの位置Right Lower
	static final double LX = 48*4;//左側マンホールのx座標
	static final double RX = 48*9;//右側マンホールのx座標
	
	static final double UpperY	= 144.0;//上段のy座標
	static final double LowerY 	= 288.0;//下段のy座標
	static final double Space	= 48.0;	//x方向の箱と箱の間隔
	static final int UP = 0;	//上段を示すサフィクス
	static final int LW = 1;	//下段を示すサフィクス
	
	static final String SndWalk = "footstep02.wav";//歩く音from魔王魂
	static final String SndDopon = "waterdopon.wav";//水に落ちる音
	static final String SndOk = "ok.wav";//マンホールを踏んだ音

	private walkingMan[][] _wm = new walkingMan[2][numOfBox];
	private byte _mh;//マンホールのフタの位置記号
	private double _mhx;//マンホールのフタのx座標
	private double _mhy;//マンホールのフタのy座標
	private int _cnter;//上段、下段の箱を順に動かすためのカウンタ
	
	@Override public void init() {
		setScWidth(WIDTH);
		setScHeight(HEIGHT);
		setBackColor(Color.LIGHTBLUE);
		setCvWidth(WIDTH);
		setCvHeight(HEIGHT);
		setFps(60);
		setIsDaemon(true);
		
		for(int i = 0; i < numOfBox; i++) {
			setwkm(UP, i, new walkingMan()); //上段_wmのインスタンス化
			setwkm(LW, i, new walkingMan()); //下段_wmのインスタンス化
			getwkm(UP, i).init(UP, i);		// _wmの初期化
			getwkm(LW, i).init(LW, i);		// _wmの初期化
		}
		set_mh(LU);				// manHoleの初期化・・最初は左上にフタを置いた
		set_cnter(UP);			// カウンタcnterの初期化・・最初は上段

	}
	
	static void SoundOk() {		//マンホールを踏んだ音
		sound snd = new sound();
		snd.str = SndOk;
		Thread th = new Thread(snd);
		th.start();
	}	
	static void SoundDopon() {	//水に落ちる音
		sound snd = new sound();
		snd.str = SndDopon;
		Thread th = new Thread(snd);
		th.start();
	}
	
	static void SoundWalk() {	//歩く音from魔王魂
		sound snd = new sound();
		snd.str = SndWalk;
		Thread th = new Thread(snd);
		th.start();
	}

	@Override protected void ofMain(GraphicsContext gc) {
		gc.clearRect(0, 0, WIDTH, HEIGHT);			// 全画面を削除
		
		//箱の位置を進める＆一番後ろの箱に50%確率で人を入れる
		int j;
		//上段の場合
		if(get_cnter()==UP) {

		SoundWalk();	//歩く音

			for(int i=0; i<numOfBox; i++) {
				getwkm(UP,i).update();	//箱の位置を進める
				if(getwkm(UP,i).get_pos()==1) {//一番後ろの箱の場合、
					if(i<7) j=i+5; else j=i+5-12;//ｊ＝5個先の箱番号
					if(getwkm(UP,j).get_isMan()) {//5個先の箱に人が入っている時
						getwkm(UP,i).set_isMan(false);//人を入れない
					}
					else {				//5個先の箱に人が居ない時
						if(Math.random()<0.5)
							getwkm(UP,i).set_isMan(false);
						else 
							getwkm(UP,i).set_isMan(true);//50%の確率で人を入れる
					}
				}
			}
			set_cnter(LW);
		}
			
		//下段の場合
		else {
			SoundWalk();	//歩く音

			for(int i=0; i<numOfBox; i++) {
				getwkm(LW,i).update();	//下段の箱の位置を進める
				if(getwkm(LW,i).get_pos()==12) {	//一番後ろの箱の場合、
					if(i<5) j=i-5+12; else j=i-5;
					if(getwkm(LW,j).get_isMan()) 	//5個先の箱に人が入っている時
						getwkm(LW,i).set_isMan(false);//人を入れない
					else {							//5個先の箱に人が居ない時
						if(Math.random()<0.5)getwkm(LW,i).set_isMan(false);
						else getwkm(LW,i).set_isMan(true);//50%の確率で人を入れる
					}
				}
			}
			set_cnter(UP);
		}
		
		//人と道路とマンホールのフタを描画
		for(int i=0;i<numOfBox;i++) {
			
			//人を描画
			getwkm(UP,i).draw(gc);//上段の箱に人が入っている場合に描画
			getwkm(LW,i).draw(gc);//下段の箱に人が入っている場合に描画

			//穴の開いた道路を描画
			gc.setFill(Color.BLACK); 	// 色を設定（黒）
			gc.setFont(new Font("System",48)); 	// フォント型、サイズを設定
			j=i+1;						//位置1～12
			if(i!=3 && i!=8) {
				gc.fillText("＿", (double)j*Space, UpperY);//上段の道路を描画
				gc.fillText("＿", (double)j*Space, LowerY);//下段の道路を描画
			}
			
		}
		//フタを描画
		gc.setFill(Color.GREEN);// 色を設定（緑色）
		gc.setFont(new Font("System Bold",48)); 	// フォントの型、サイズを設定
		gc.fillText("＿", get_mhx(), get_mhy());		// フタを描画
	}
	
	//押したキーに応じて、マンホールの位置を更新して描画
	@Override protected void ofKeyPressed(KeyEvent e, GraphicsContext gc) {
		//水色でフタを上書き
		gc.setFill(Color.LIGHTBLUE);// 色を設定（水色）
		gc.setFont(new Font("System Bold",48)); 	// フォント型、サイズを設定
		gc.fillText("＿", get_mhx(), get_mhy());		// フタを水色で上書き
		
		gc.setFill(Color.GREEN); 					// 色を設定（緑色）
		gc.setFont(new Font("System Bold",48)); 	// フォント型、サイズを設定
		switch(e.getCode()) {
		case Q:										
			set_mh(LU); gc.fillText("＿", LX, UpperY); break;
		case A:
			set_mh(LL); gc.fillText("＿", LX, LowerY); break;
		case P:
			set_mh(RU); gc.fillText("＿", RX, UpperY); break;
		case L:
			set_mh(RL); gc.fillText("＿", RX, LowerY); break;
		case ENTER: Platform.exit(); break;
		default: gc.fillText("＿", get_mhx(), get_mhy()); break;
		}
	}
	
	
	@Override protected void ofKeyReleased(KeyEvent e) {
	}

	//Media mの音を鳴らす
//	private void playMedia(Media m){
//	       if (m != null){
//	       }
//	}

	//	ゲッター(getter)/セッター(setter)
	public walkingMan getwkm(int line, int index) {
		return _wm[line][index];
	}
	public walkingMan[][] getwkms() {
		return _wm;
	}
	public void setwkm(int line, int index, walkingMan value) {
			_wm[line][index] = value;
	}

	public byte get_mh() {
		return _mh;
	}
	public void set_mh(byte _mh) {
		this._mh = _mh;
		set_mhx(_mh);
		set_mhy(_mh);
	}

	public int get_cnter() {
		return _cnter;
	}
	public void set_cnter(int _cnter) {
		this._cnter = _cnter;
	}

	public double get_mhx() {
		return _mhx;
	}
	public void set_mhx(byte index) {
		if(index==LU || index==LL) this._mhx = LX;
		else this._mhx = RX;
	}

	public double get_mhy() {
		return _mhy;
	}
	public void set_mhy(byte index) {
		if(index==LU || index==RU) this._mhy = UpperY;
		else this._mhy = LowerY;
	}
}
