package com.secure.documentwallet;
import android.content.Context;import android.net.Uri;import com.google.android.gms.tasks.Tasks;import com.google.mlkit.vision.common.InputImage;import com.google.mlkit.vision.text.*;import com.google.mlkit.vision.text.latin.TextRecognizerOptions;import java.util.List;import java.util.concurrent.TimeUnit;
final class OcrIndexer {
 static String read(Context c,List<Uri> pages){if(pages==null||pages.isEmpty())return "";StringBuilder out=new StringBuilder();TextRecognizer recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);try{for(Uri page:pages){try{Text text=Tasks.await(recognizer.process(InputImage.fromFilePath(c,page)),25,TimeUnit.SECONDS);String value=text.getText().trim();if(!value.isEmpty()){if(out.length()>0)out.append('\n');out.append(value);}if(out.length()>24000)break;}catch(Exception ignored){}}}finally{recognizer.close();}return out.length()>24000?out.substring(0,24000):out.toString();}
}
