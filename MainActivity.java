package com.binaa.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
  DB db; LinearLayout root, body, nav; String page="الرئيسية"; boolean dark=false;
  int green=Color.rgb(31,125,93), ink=Color.rgb(34,48,43), muted=Color.rgb(112,126,119), bg=Color.rgb(245,248,246), card=Color.WHITE;
  public void onCreate(Bundle b){super.onCreate(b); db=new DB(this); render();}
  int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
  TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(null,Typeface.BOLD);t.setPadding(dp(4),dp(5),dp(4),dp(5));return t;}
  GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
  LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
  void render(){
    bg=dark?Color.rgb(24,31,29):Color.rgb(245,248,246); card=dark?Color.rgb(37,47,43):Color.WHITE; ink=dark?Color.WHITE:Color.rgb(34,48,43); muted=dark?Color.rgb(181,194,187):Color.rgb(112,126,119);
    root=col();root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(bg);setContentView(root);
    LinearLayout header=col();header.setPadding(dp(18),dp(22),dp(18),dp(12)); TextView brand=text("بِنـاء",25,green,true);header.addView(brand);header.addView(text("إدارة تكاليف تشطيب الشقق",13,muted,false));root.addView(header);
    body=col();body.setPadding(dp(16),dp(6),dp(16),dp(10)); ScrollView scroll=new ScrollView(this);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    nav=row();nav.setPadding(dp(5),dp(8),dp(5),dp(8));nav.setBackgroundColor(card);String[] pages={"الرئيسية","المشاريع","المواد","العمال","التكاليف"};for(String p:pages){TextView t=text(p,12,page.equals(p)?green:muted,true);t.setGravity(Gravity.CENTER);t.setOnClickListener(v->{page=p;render();});nav.addView(t,new LinearLayout.LayoutParams(0,dp(48),1));}root.addView(nav);
    if(page.equals("الرئيسية")) dashboard(); else if(page.equals("المشاريع")) listing("projects");else if(page.equals("المواد"))listing("materials");else if(page.equals("العمال"))listing("workers");else costs();
  }
  void panel(String title,String value){LinearLayout p=col();p.setPadding(dp(16),dp(13),dp(16),dp(13));p.setBackground(bg(card,16));TextView a=text(title,13,muted,false),b=text(value,23,ink,true);p.addView(a);p.addView(b);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(10);body.addView(p,lp);}
  void dashboard(){TextView hi=text("أهلاً بك 👋",22,ink,true);body.addView(hi);body.addView(text("ملخص أعمالك وتكاليف مشاريعك",13,muted,false));Space sp=new Space(this);body.addView(sp,new LinearLayout.LayoutParams(1,dp(12)));
    panel("المشاريع المسجلة",db.count("projects"));panel("المواد في المكتبة",db.count("materials"));panel("العمال والحرفيون",db.count("workers"));panel("إجمالي التكاليف المسجلة",money(db.total()));
    TextView hint=text("ابدأ بإضافة مشروع، ثم سجّل المواد والعمال وأضف بنود التكلفة من تبويب التكاليف.",14,muted,false);hint.setPadding(dp(8),dp(12),dp(8),dp(12));body.addView(hint);
    Button theme=button(dark?"تفعيل الوضع الفاتح":"تفعيل الوضع الليلي");theme.setOnClickListener(v->{dark=!dark;render();});body.addView(theme);
  }
  Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackground(bg(green,14));return b;}
  String tableTitle(String t){return t.equals("projects")?"مشروع":t.equals("materials")?"مادة":"عامل";}
  void listing(String table){LinearLayout top=row();TextView title=text(table.equals("projects")?"مشاريعي":table.equals("materials")?"مكتبة المواد":"العمال والحرفيون",20,ink,true);top.addView(title,new LinearLayout.LayoutParams(0,-2,1));Button add=button("+ إضافة");add.setOnClickListener(v->addDialog(table));top.addView(add);body.addView(top);body.addView(text("اضغط على أي سجل لتعديله أو حذفه.",12,muted,false));
    Cursor c=db.get(table);if(c.getCount()==0){body.addView(text("لا توجد بيانات بعد. اضغط على إضافة للبدء.",15,muted,false));}while(c.moveToNext()){final long id=c.getLong(0);String name=c.getString(1);String sub="";if(table.equals("projects"))sub="المساحة: "+c.getString(2)+" م²  •  "+c.getString(3);if(table.equals("materials"))sub=c.getString(2)+" • "+c.getString(3)+" • "+money(c.getDouble(4));if(table.equals("workers"))sub=c.getString(2)+" • "+money(c.getDouble(3))+" / "+c.getString(4);
      LinearLayout p=col();p.setPadding(dp(14),dp(9),dp(14),dp(9));p.setBackground(bg(card,14));p.addView(text(name,16,ink,true));p.addView(text(sub,12,muted,false));p.setOnClickListener(v->editDialog(table,id));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(9);body.addView(p,lp);}c.close();
  }
  void addDialog(String table){formDialog(table,-1);}
  void editDialog(String table,long id){formDialog(table,id);}
  void formDialog(String table,long id){String[] labels=table.equals("projects")?new String[]{"اسم المشروع","المساحة بالمتر المربع","الموقع","ملاحظات"}:table.equals("materials")?new String[]{"اسم المادة","التصنيف / الماركة","وحدة القياس","سعر الوحدة","المورد"}:new String[]{"اسم العامل","المهنة","الأجرة","وحدة الأجرة (يومية/متر/قطعة)","رقم الهاتف"};EditText[] e=new EditText[labels.length];LinearLayout form=col();form.setPadding(dp(8),dp(4),dp(8),dp(4));
    if(id!=-1){Cursor c=db.one(table,id);if(c.moveToFirst()){for(int i=0;i<labels.length;i++){String val="";int ix=i+1;if(table.equals("projects")&&i==3)ix=4;if(table.equals("materials")&&i==4)ix=5;if(table.equals("workers")&&i==4)ix=5;val=c.isNull(ix)?"":c.getString(ix);e[i]=field(form,labels[i],val);} }c.close();}else for(int i=0;i<labels.length;i++)e[i]=field(form,labels[i],"");
    AlertDialog.Builder ab=new AlertDialog.Builder(this).setTitle(id==-1?"إضافة "+tableTitle(table):"تعديل "+tableTitle(table)).setView(form).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{String[] vals=new String[e.length];for(int i=0;i<e.length;i++)vals[i]=e[i].getText().toString().trim();if(vals[0].isEmpty()){toast("يرجى إدخال الاسم");return;}db.save(table,id,vals);toast("تم الحفظ بنجاح");render();});if(id!=-1)ab.setNeutralButton("حذف",(d,w)->new AlertDialog.Builder(this).setMessage("هل تريد حذف هذا السجل؟").setNegativeButton("إلغاء",null).setPositiveButton("حذف",(x,y)->{db.delete(table,id);render();}).show());ab.show();
  }
  EditText field(LinearLayout f,String label,String value){f.addView(text(label,12,muted,true));EditText e=new EditText(this);e.setText(value);e.setSingleLine(true);e.setTextSize(15);e.setPadding(dp(10),dp(5),dp(10),dp(5));e.setBackground(bg(dark?Color.rgb(51,63,57):Color.rgb(242,246,243),10));f.addView(e,new LinearLayout.LayoutParams(-1,dp(48)));return e;}
  void costs(){TextView h=text("بنود التكاليف",21,ink,true);body.addView(h);Button add=button("+ إضافة بند تكلفة");add.setOnClickListener(v->costDialog());body.addView(add);panel("إجمالي التكلفة",money(db.total()));Cursor c=db.costs();if(c.getCount()==0)body.addView(text("لم تسجل أي تكاليف بعد.",14,muted,false));while(c.moveToNext()){LinearLayout p=col();p.setPadding(dp(13),dp(10),dp(13),dp(10));p.setBackground(bg(card,14));p.addView(text(c.getString(1),15,ink,true));p.addView(text(c.getString(2)+" • كمية: "+c.getDouble(3)+" • سعر الوحدة: "+money(c.getDouble(4)),12,muted,false));p.addView(text("الإجمالي: "+money(c.getDouble(3)*c.getDouble(4)),14,green,true));final long id=c.getLong(0);p.setOnLongClickListener(v->{new AlertDialog.Builder(this).setMessage("حذف بند التكلفة؟").setNegativeButton("إلغاء",null).setPositiveButton("حذف",(a,b)->{db.deleteCost(id);render();}).show();return true;});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(8);body.addView(p,lp);}c.close();body.addView(text("ملاحظة: اضغط مطولاً على بند التكلفة لحذفه.",12,muted,false));}
  void costDialog(){LinearLayout f=col();f.setPadding(dp(8),dp(4),dp(8),dp(4));EditText name=field(f,"وصف البند",""),qty=field(f,"الكمية","1"),price=field(f,"سعر الوحدة","0");new AlertDialog.Builder(this).setTitle("إضافة بند تكلفة").setView(f).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{try{if(name.getText().toString().trim().isEmpty())throw new Exception();double q=Double.parseDouble(qty.getText().toString()),p=Double.parseDouble(price.getText().toString());if(q<0||p<0)throw new Exception();db.addCost(name.getText().toString(),"عام",q,p);render();}catch(Exception ex){toast("تأكد من الوصف والكمية والسعر");}}).show();}
  String money(double v){return String.format(Locale.US,"%,.0f ل.س",v);}void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
  static class DB extends SQLiteOpenHelper{
    DB(Context c){super(c,"binaa_local.db",null,1);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE projects(_id INTEGER PRIMARY KEY,name TEXT,area TEXT,location TEXT,notes TEXT)");d.execSQL("CREATE TABLE materials(_id INTEGER PRIMARY KEY,name TEXT,brand TEXT,unit TEXT,price REAL,supplier TEXT)");d.execSQL("CREATE TABLE workers(_id INTEGER PRIMARY KEY,name TEXT,job TEXT,wage REAL,unit TEXT,phone TEXT)");d.execSQL("CREATE TABLE costs(_id INTEGER PRIMARY KEY,name TEXT,category TEXT,qty REAL,price REAL)");}public void onUpgrade(SQLiteDatabase d,int a,int b){}
    String count(String t){Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+t,null);c.moveToFirst();String s=c.getString(0);c.close();return s;}
    Cursor get(String t){return getReadableDatabase().rawQuery("SELECT * FROM "+t+" ORDER BY _id DESC",null);}Cursor one(String t,long id){return getReadableDatabase().rawQuery("SELECT * FROM "+t+" WHERE _id="+id,null);}void delete(String t,long id){getWritableDatabase().delete(t,"_id=?",new String[]{""+id});}
    void save(String t,long id,String[] v){ContentValues c=new ContentValues();if(t.equals("projects")){c.put("name",v[0]);c.put("area",v[1]);c.put("location",v[2]);c.put("notes",v[3]);}else if(t.equals("materials")){c.put("name",v[0]);c.put("brand",v[1]);c.put("unit",v[2]);c.put("price",num(v[3]));c.put("supplier",v[4]);}else{c.put("name",v[0]);c.put("job",v[1]);c.put("wage",num(v[2]));c.put("unit",v[3]);c.put("phone",v[4]);}if(id==-1)getWritableDatabase().insert(t,null,c);else getWritableDatabase().update(t,c,"_id=?",new String[]{""+id});}
    double num(String s){try{return Double.parseDouble(s);}catch(Exception e){return 0;}}void addCost(String n,String cat,double q,double p){ContentValues c=new ContentValues();c.put("name",n);c.put("category",cat);c.put("qty",q);c.put("price",p);getWritableDatabase().insert("costs",null,c);}Cursor costs(){return getReadableDatabase().rawQuery("SELECT * FROM costs ORDER BY _id DESC",null);}double total(){Cursor c=getReadableDatabase().rawQuery("SELECT SUM(qty*price) FROM costs",null);c.moveToFirst();double n=c.isNull(0)?0:c.getDouble(0);c.close();return n;}void deleteCost(long id){getWritableDatabase().delete("costs","_id=?",new String[]{""+id});}
  }
}
