package com.naskull.aiphoto

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color as C
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private val Ink=C(0xFF101114); private val Panel=C(0xFF1B1D22); private val Gold=C(0xFFC5A46D); private val Muted=C(0xFFAAADB5)
private val Scenes=listOf("Auto IA","Comida","Retrato","Noche","Paisaje")
private val Filters=listOf("Original","Auto IA","Noche HDR","Comida","Retrato","Noche","Paisaje","B/N","Vintage","Cálido","Frío")
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent { MaterialTheme(colorScheme=darkColorScheme(primary=Gold,background=Ink,surface=Panel)){ PhotoApp() } } } }

@Composable private fun PhotoApp(){
 var scene by remember { mutableStateOf("Auto IA") }; var strength by remember { mutableFloatStateOf(.7f) }; var tab by remember { mutableIntStateOf(0) }
 Surface(Modifier.fillMaxSize(),color=Ink){ Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=16.dp,vertical=14.dp)) {
  Row(verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(38.dp).background(Gold,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Icon(Icons.Rounded.AutoAwesome,null,tint=Ink)}; Spacer(Modifier.width(10.dp)); Column { Text("NASKULL",fontSize=19.sp,fontWeight=FontWeight.Black,color=C.White,letterSpacing=2.sp); Text("AI PHOTO  •  VERSIÓN 0.3",fontSize=9.sp,color=Gold,letterSpacing=1.5.sp) } }
  Spacer(Modifier.height(12.dp)); Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.horizontalScroll(rememberScrollState())){ FilterChip(selected=tab==0,onClick={tab=0},label={Text("Cámara")},leadingIcon={Icon(Icons.Rounded.CameraAlt,null,Modifier.size(16.dp))}); FilterChip(selected=tab==1,onClick={tab=1},label={Text("Editar foto")},leadingIcon={Icon(Icons.Rounded.PhotoLibrary,null,Modifier.size(16.dp))}); FilterChip(selected=tab==2,onClick={tab=2},label={Text("Consejos")},leadingIcon={Icon(Icons.Rounded.Tune,null,Modifier.size(16.dp))}) }
  Spacer(Modifier.height(10.dp))
  when(tab){ 0->CameraTab(scene,strength,{scene=it},{strength=it}); 1->EditorTab(scene,strength,{scene=it},{strength=it}); else->TipsTab(scene){scene=it} }
  Spacer(Modifier.height(24.dp));HorizontalDivider(color=C(0xFF303138));Spacer(Modifier.height(12.dp));Text("NASKULL AI PHOTO  •  Hecho para crear mejores imágenes",color=Muted,fontSize=10.sp)
} } }

@Composable private fun FilterControls(scene:String,strength:Float,onScene:(String)->Unit,onStrength:(Float)->Unit){
 Text("FILTRO",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp); Spacer(Modifier.height(6.dp)); Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){Filters.forEach{FilterChip(selected=scene==it,onClick={onScene(it)},label={Text(it,fontSize=12.sp)})}}
 Spacer(Modifier.height(4.dp)); Row(verticalAlignment=Alignment.CenterVertically){ Text("Intensidad",color=C.White,fontSize=13.sp); Slider(value=strength,onValueChange=onStrength,valueRange=.1f..1f,modifier=Modifier.weight(1f).padding(horizontal=10.dp)); Text("${(strength*100).toInt()}%",color=Muted,fontSize=12.sp) }
}

@Composable private fun CameraTab(scene:String,strength:Float,onScene:(String)->Unit,onStrength:(Float)->Unit){
 val context=LocalContext.current; val owner=LocalLifecycleOwner.current; val scope=rememberCoroutineScope()
 var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
 val ask=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ granted=it }
 LaunchedEffect(Unit){ if(!granted) ask.launch(Manifest.permission.CAMERA) }
 var back by remember { mutableStateOf(true) }; var nightExt by remember { mutableStateOf(false) }; var nightAvailable by remember { mutableStateOf(false) }; var keepOriginal by remember { mutableStateOf(false) }; var busy by remember { mutableStateOf(false) }; var last by remember { mutableStateOf<Bitmap?>(null) }
 val previewView=remember { PreviewView(context).apply{ implementationMode=PreviewView.ImplementationMode.COMPATIBLE; scaleType=PreviewView.ScaleType.FILL_CENTER } }
 val capture=remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build() }
 // Usa el modo Noche del propio fabricante (CameraX Extensions) cuando el móvil lo ofrece a otras apps.
 DisposableEffect(granted,back,nightExt){ var provider:ProcessCameraProvider?=null
  if(granted){ val exec=ContextCompat.getMainExecutor(context); val pf=ProcessCameraProvider.getInstance(context); pf.addListener({ val p=pf.get(); provider=p; val base=if(back)CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA; val ef=ExtensionsManager.getInstanceAsync(context,p); ef.addListener({
   val selector=try{ val em=ef.get(); nightAvailable=em.isExtensionAvailable(base,ExtensionMode.NIGHT); if(nightExt&&nightAvailable) em.getExtensionEnabledCameraSelector(base,ExtensionMode.NIGHT) else base }catch(_:Exception){ base }
   try{ p.unbindAll(); p.bindToLifecycle(owner,selector,Preview.Builder().build().also{it.setSurfaceProvider(previewView.surfaceProvider)},capture) }catch(_:Exception){ Toast.makeText(context,"No se pudo abrir la cámara",Toast.LENGTH_SHORT).show() } },exec) },exec) }
  onDispose { provider?.unbindAll() } }
 fun shoot(){ val f=File(File(context.cacheDir,"camera").apply{mkdirs()},"raw_${System.currentTimeMillis()}.jpg"); busy=true
  capture.takePicture(ImageCapture.OutputFileOptions.Builder(f).build(),ContextCompat.getMainExecutor(context),object:ImageCapture.OnImageSavedCallback{
   override fun onImageSaved(output:ImageCapture.OutputFileResults){ scope.launch{ val result=withContext(Dispatchers.Default){ try{ loadBitmap(context,Uri.fromFile(f))?.let{ src -> val out=enhance(src,strength,scene); if(keepOriginal) saveToGallery(context,src,"ORIG_"); if(saveToGallery(context,out,"")) out else null } }catch(_:Exception){ null } }; f.delete(); busy=false; if(result!=null){ last=result; Toast.makeText(context,"Guardada en Pictures/NASKULL AI Photo",Toast.LENGTH_SHORT).show() } else Toast.makeText(context,"No se pudo guardar la foto",Toast.LENGTH_SHORT).show() } }
   override fun onError(exception:ImageCaptureException){ busy=false; Toast.makeText(context,"Error al hacer la foto",Toast.LENGTH_SHORT).show() } }) }
 Box(Modifier.fillMaxWidth().aspectRatio(3f/4f).clip(RoundedCornerShape(18.dp)).background(C.Black),contentAlignment=Alignment.Center){
  if(granted) AndroidView(factory={previewView},modifier=Modifier.fillMaxSize(),update={ it.setLayerType(View.LAYER_TYPE_HARDWARE,Paint().apply{ colorFilter=ColorMatrixColorFilter(previewMatrix(scene,strength)) }) })
  else Column(horizontalAlignment=Alignment.CenterHorizontally){ Text("La app necesita permiso de cámara",color=C.White); Spacer(Modifier.height(10.dp)); Button(onClick={ask.launch(Manifest.permission.CAMERA)}){ Text("DAR PERMISO") } }
  Text(scene,color=C.White,fontSize=12.sp,fontWeight=FontWeight.Bold,modifier=Modifier.align(Alignment.TopStart).padding(10.dp).background(Ink.copy(alpha=.6f),RoundedCornerShape(8.dp)).padding(horizontal=8.dp,vertical=4.dp))
  if(busy) Box(Modifier.fillMaxSize().background(C.Black.copy(alpha=.45f)),contentAlignment=Alignment.Center){ Column(horizontalAlignment=Alignment.CenterHorizontally){ CircularProgressIndicator(); Spacer(Modifier.height(8.dp)); Text("Aplicando filtro…",color=C.White) } }
 }
 Spacer(Modifier.height(12.dp)); FilterControls(scene,strength,onScene,onStrength)
 Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){ if(nightAvailable) FilterChip(selected=nightExt,onClick={nightExt=!nightExt},label={Text("Modo noche del móvil",fontSize=12.sp)},leadingIcon={Icon(Icons.Rounded.NightsStay,null,Modifier.size(16.dp))}); FilterChip(selected=keepOriginal,onClick={keepOriginal=!keepOriginal},label={Text("Guardar también original",fontSize=12.sp)}) }
 Spacer(Modifier.height(10.dp))
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
  Box(Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Panel)){ last?.let{ Image(it.asImageBitmap(),"Última foto",Modifier.fillMaxSize(),contentScale=ContentScale.Crop) } }
  Box(Modifier.size(78.dp).border(4.dp,C.White,CircleShape).padding(8.dp).clip(CircleShape).background(if(busy||!granted)Muted else Gold).clickable(enabled=granted&&!busy){ shoot() })
  IconButton(onClick={back=!back},modifier=Modifier.size(56.dp)){ Icon(Icons.Rounded.Cameraswitch,"Cambiar cámara",tint=C.White,modifier=Modifier.size(30.dp)) }
 }
 Spacer(Modifier.height(10.dp)); Text("La vista previa es aproximada; el filtro completo (como Noche HDR) se aplica al hacer la foto.",color=Muted,fontSize=11.sp,lineHeight=16.sp)
}

@Composable private fun EditorTab(scene:String,strength:Float,onScene:(String)->Unit,onStrength:(Float)->Unit){
 val context=LocalContext.current; val scope=rememberCoroutineScope(); var original by remember { mutableStateOf<Bitmap?>(null) }; var edited by remember { mutableStateOf<Bitmap?>(null) }; var busy by remember { mutableStateOf(false) }
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri -> if(uri!=null) try { loadBitmap(context,uri)?.let { original=it; edited=null } ?: Toast.makeText(context,"No se pudo abrir la imagen",Toast.LENGTH_SHORT).show() } catch(_:Exception){ Toast.makeText(context,"No se pudo abrir la imagen",Toast.LENGTH_SHORT).show() } }
 val preview=edited?:original
 if(preview!=null) Image(preview.asImageBitmap(),null,Modifier.fillMaxWidth().heightIn(max=430.dp).border(1.dp,if(edited!=null)Gold else Muted,RoundedCornerShape(18.dp))) else Box(Modifier.fillMaxWidth().height(165.dp).background(Panel,RoundedCornerShape(18.dp)).border(1.dp,C(0xFF34363D),RoundedCornerShape(18.dp)).clickable{picker.launch("image/*")},contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Rounded.Image,null,tint=Gold,modifier=Modifier.size(34.dp));Spacer(Modifier.height(8.dp));Text("Toca para elegir una foto",color=C.White,fontWeight=FontWeight.Medium);Text("JPG, PNG y otras imágenes del móvil",color=Muted,fontSize=12.sp)}}
 Spacer(Modifier.height(8.dp)); OutlinedButton(onClick={picker.launch("image/*")},modifier=Modifier.fillMaxWidth()){ Icon(Icons.Rounded.PhotoLibrary,null); Spacer(Modifier.width(8.dp)); Text("ELEGIR DE LA GALERÍA") }
 Spacer(Modifier.height(12.dp)); FilterControls(scene,strength,onScene,onStrength)
 Spacer(Modifier.height(8.dp)); Button(enabled=!busy,onClick={val src=original; if(src==null)Toast.makeText(context,"Primero elige una foto",Toast.LENGTH_SHORT).show() else scope.launch{busy=true;edited=withContext(Dispatchers.Default){enhance(src,strength,scene)};busy=false}},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp)){if(busy)CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp) else Icon(Icons.Rounded.AutoAwesome,null);Spacer(Modifier.width(8.dp));Text(if(busy)"APLICANDO…" else "APLICAR FILTRO",fontWeight=FontWeight.Bold)}
 if(edited!=null){Spacer(Modifier.height(8.dp));OutlinedButton(onClick={ Toast.makeText(context,if(saveToGallery(context,edited!!,""))"Guardada en Pictures/NASKULL AI Photo" else "No se pudo guardar",Toast.LENGTH_LONG).show() },modifier=Modifier.fillMaxWidth().height(50.dp)){Text("GUARDAR FOTO")}}
}

@Composable private fun TipsTab(scene:String,onScene:(String)->Unit){
 Text("ASISTENTE PARA XIAOMI 17 ULTRA",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){Scenes.forEach{FilterChip(selected=scene==it,onClick={onScene(it)},label={Text(it,fontSize=12.sp)})}}; Spacer(Modifier.height(12.dp))
 val tips=when(scene){"Comida"->listOf("Usa la cámara principal 1× (23 mm equivalente).","Busca luz lateral suave, por ejemplo junto a una ventana.","Toca el plato para enfocar y reduce algo la exposición si brilla demasiado.","Evita acercarte demasiado para no deformar la comida.");"Retrato"->listOf("Prueba el teleobjetivo óptico 3× si tienes espacio.","Sitúa a la persona cerca de una ventana.","Separa al sujeto del fondo para lograr desenfoque natural.","Evita filtros de belleza intensos.");"Noche","Noche HDR"->listOf("Usa la cámara principal 1× para captar más luz.","Apoya el móvil o usa trípode.","Usa el filtro Noche HDR para recuperar las sombras.","Activa «Modo noche del móvil» en la cámara si aparece.");"Paisaje"->listOf("Usa 1× para máxima calidad o ultra gran angular para escenas amplias.","Activa la cuadrícula y alinea el horizonte.","En atardeceres, toca el cielo y baja un poco la exposición.","Limpia la lente antes de disparar.");else->listOf("Limpia la lente antes de hacer la foto.","Toca el sujeto principal para enfocar.","Baja un poco la exposición si las luces se queman.","Usa 1× para calidad o el teleobjetivo óptico para detalles lejanos.")}
 tips.forEachIndexed{i,t->Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.Top){Box(Modifier.size(28.dp).background(Gold.copy(alpha=.16f),RoundedCornerShape(9.dp)),contentAlignment=Alignment.Center){Text("${i+1}",color=Gold,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(10.dp));Text(t,color=C.White,fontSize=14.sp,lineHeight=20.sp)}}
}

private fun saveToGallery(context:Context,bmp:Bitmap,tag:String):Boolean{ return try{ val v=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"NASKULL_$tag${System.currentTimeMillis()}.jpg");put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/NASKULL AI Photo")}; val u=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v)?:return false; context.contentResolver.openOutputStream(u)?.use{bmp.compress(Bitmap.CompressFormat.JPEG,95,it)}!=null }catch(_:Exception){ false } }

// Reduce las fotos grandes (p. ej. 50 MP) para no agotar la memoria y corrige la rotación EXIF de la cámara.
private fun loadBitmap(context:Context,uri:Uri,maxSide:Int=3000):Bitmap?{val r=context.contentResolver;val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};r.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,bounds)};var sample=1;while(maxOf(bounds.outWidth,bounds.outHeight)/sample>maxSide)sample*=2;val bmp=r.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})}?:return null;val rotation=r.openInputStream(uri)?.use{when(ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)){ExifInterface.ORIENTATION_ROTATE_90->90f;ExifInterface.ORIENTATION_ROTATE_180->180f;ExifInterface.ORIENTATION_ROTATE_270->270f;else->0f}}?:0f;return if(rotation==0f)bmp else Bitmap.createBitmap(bmp,0,0,bmp.width,bmp.height,Matrix().apply{postRotate(rotation)},true)}

// Aproximación en tiempo real de cada filtro para la vista previa de la cámara.
private fun previewMatrix(scene:String,a:Float):ColorMatrix{ var sat=1f;var con=1f;var lift=0f;var rs=1f;var gs=1f;var bs=1f
 when(scene){"Original"->{};"Noche HDR"->{lift=70f*a;con=.9f;sat=1f+.35f*a;bs=1f+.05f*a};"B/N"->{sat=1f-(.4f+.6f*a);con=1f+.18f*a};"Vintage"->{sat=1f-.5f*a;rs=1f+.1f*a;bs=1f-.12f*a;lift=10f*a};"Cálido"->{rs=1f+.09f*a;gs=1f+.03f*a;bs=1f-.09f*a};"Frío"->{rs=1f-.07f*a;bs=1f+.09f*a};"Noche"->{con=1f+.18f*a;lift=12f};else->{con=1f+.18f*a;sat=1f+.1f*a}}
 val t=128f*(1f-con)+lift
 return ColorMatrix().apply{ setSaturation(sat); postConcat(ColorMatrix(floatArrayOf(con*rs,0f,0f,0f,t, 0f,con*gs,0f,0f,t, 0f,0f,con*bs,0f,t, 0f,0f,0f,1f,0f))) } }

private fun isDark(src:Bitmap):Boolean{ val s=Bitmap.createScaledBitmap(src,32,32,true); var sum=0f; for(y in 0 until 32) for(x in 0 until 32){ val c=s.getPixel(x,y); sum+=(.299f*Color.red(c)+.587f*Color.green(c)+.114f*Color.blue(c))/255f }; return sum/1024f<.25f }

// Noche HDR: aclara las sombras según la luz de cada zona (no de forma global), realza el detalle y da color al cielo nocturno.
private fun nightHdr(src:Bitmap,a:Float):Bitmap{ val w=src.width;val h=src.height; var s=src; while(max(s.width,s.height)>64) s=Bitmap.createScaledBitmap(s,max(1,s.width/2),max(1,s.height/2),true)
 val base=IntArray(w*h); Bitmap.createScaledBitmap(s,w,h,true).getPixels(base,0,w,0,0,w,h); val px=IntArray(w*h); src.getPixels(px,0,w,0,0,w,h)
 val sat=1f+.35f*a; val maxGain=1f+5f*a; val gamma=1f-.65f*a; val detail=1f+.5f*a
 for(i in px.indices){ val c=px[i]; var r=Color.red(c)/255f; var g=Color.green(c)/255f; var b=Color.blue(c)/255f; val l=.299f*r+.587f*g+.114f*b; val bc=base[i]; val bl=(.299f*Color.red(bc)+.587f*Color.green(bc)+.114f*Color.blue(bc))/255f
  val lift=bl.pow(gamma); val bp=lift+(bl-lift)*min(1f,bl*1.6f); val nl=bp*((l+.01f)/(bl+.01f)).pow(detail); val gain=min(nl/(l+.004f),maxGain); r*=gain;g*=gain;b*=gain
  val l2=.299f*r+.587f*g+.114f*b; r=l2+(r-l2)*sat; g=l2+(g-l2)*sat; b=l2+(b-l2)*sat; val sh=max(0f,1f-l2); b+=.06f*a*sh*sh; r+=.03f*a*min(1f,l2)
  px[i]=Color.rgb((r*255f).toInt().coerceIn(0,255),(g*255f).toInt().coerceIn(0,255),(b*255f).toInt().coerceIn(0,255)) }
 return Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{it.setPixels(px,0,w,0,0,w,h)} }

private fun enhance(src:Bitmap,amount:Float,scene:String):Bitmap{if(scene=="Original")return src;if(scene=="Noche HDR"||(scene=="Auto IA"&&isDark(src)))return nightHdr(src,amount);val w=src.width;val h=src.height;val pixels=IntArray(w*h);src.getPixels(pixels,0,w,0,0,w,h);val contrast=1f+.18f*amount;val brightness=if(scene=="Noche")1.05f else 1.015f;for(i in pixels.indices){val c=pixels[i];var r=((Color.red(c)-128f)*contrast+128f)*brightness;var g=((Color.green(c)-128f)*contrast+128f)*brightness;var b=((Color.blue(c)-128f)*contrast+128f)*brightness;when(scene){
 "Comida"->{r*=1f+.035f*amount;g*=1f+.012f*amount}
 "Paisaje"->{g*=1f+.025f*amount;b*=1f+.02f*amount}
 "Retrato"->r*=1f+.012f*amount
 "B/N"->{val l=.299f*r+.587f*g+.114f*b;val t=.4f+.6f*amount;r+=(l-r)*t;g+=(l-g)*t;b+=(l-b)*t}
 "Vintage"->{val sr=.393f*r+.769f*g+.189f*b;val sg=.349f*r+.686f*g+.168f*b;val sb=.272f*r+.534f*g+.131f*b;val t=.8f*amount;r+=(sr-r)*t+12f*amount;g+=(sg-g)*t+8f*amount;b+=(sb-b)*t+10f*amount}
 "Cálido"->{r*=1f+.09f*amount;g*=1f+.03f*amount;b*=1f-.09f*amount}
 "Frío"->{r*=1f-.07f*amount;b*=1f+.09f*amount}
};pixels[i]=Color.rgb(r.toInt().coerceIn(0,255),g.toInt().coerceIn(0,255),b.toInt().coerceIn(0,255))};return Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{it.setPixels(pixels,0,w,0,0,w,h)}}
