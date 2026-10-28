package com.naskull.aiphoto

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color as C
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val Ink=C(0xFF101114); private val Panel=C(0xFF1B1D22); private val Gold=C(0xFFC5A46D); private val Muted=C(0xFFAAADB5)
private val Scenes=listOf("Auto IA","Comida","Retrato","Noche","Paisaje")
private val Filters=Scenes+listOf("B/N","Vintage","Cálido","Frío")
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent { MaterialTheme(colorScheme=darkColorScheme(primary=Gold,background=Ink,surface=Panel)){ PhotoApp() } } } }

@Composable private fun PhotoApp(){
 val context= LocalContext.current; val scope=rememberCoroutineScope(); var original by remember { mutableStateOf<Bitmap?>(null) }; var edited by remember { mutableStateOf<Bitmap?>(null) }; var scene by remember { mutableStateOf("Auto IA") }; var strength by remember { mutableFloatStateOf(.55f) }; var tab by remember { mutableIntStateOf(0) }; var busy by remember { mutableStateOf(false) }
 fun open(uri:Uri){ try { loadBitmap(context,uri)?.let { original=it; edited=null; tab=0 } ?: Toast.makeText(context,"No se pudo abrir la imagen",Toast.LENGTH_SHORT).show() } catch(_:Exception){ Toast.makeText(context,"No se pudo abrir la imagen",Toast.LENGTH_SHORT).show() } }
 val picker= rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri -> if(uri!=null) open(uri) }
 val photoUri= remember { val dir=File(context.cacheDir,"camera").apply{mkdirs()}; FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",File(dir,"captura.jpg")) }
 val camera= rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){ ok -> if(ok) open(photoUri) }
 fun takePhoto(){ try { camera.launch(photoUri) } catch(_:Exception){ Toast.makeText(context,"No se encontró una app de cámara",Toast.LENGTH_SHORT).show() } }
 Surface(Modifier.fillMaxSize(),color=Ink){ Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
  Row(verticalAlignment=Alignment.CenterVertically){ Box(Modifier.size(44.dp).background(Gold,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center){Icon(Icons.Rounded.AutoAwesome,null,tint=Ink)}; Spacer(Modifier.width(12.dp)); Column { Text("NASKULL",fontSize=22.sp,fontWeight=FontWeight.Black,color=C.White,letterSpacing=2.sp); Text("AI PHOTO  •  VERSIÓN 0.2",fontSize=10.sp,color=Gold,letterSpacing=1.5.sp) } }
  Spacer(Modifier.height(24.dp)); Text("Haz que cada foto\ncuente una historia.",fontSize=30.sp,lineHeight=35.sp,fontWeight=FontWeight.Bold,color=C.White); Spacer(Modifier.height(8.dp)); Text("Haz una foto o elige una de la galería y aplica un filtro.",color=Muted,fontSize=14.sp); Spacer(Modifier.height(20.dp))
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){ FeatureCard("HACER FOTO","Con la cámara del móvil",Icons.Rounded.CameraAlt,Modifier.weight(1f)){takePhoto()}; FeatureCard("GALERÍA","Elegir una imagen",Icons.Rounded.PhotoLibrary,Modifier.weight(1f)){picker.launch("image/*")} }
  Spacer(Modifier.height(18.dp)); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ FilterChip(selected=tab==0,onClick={tab=0},label={Text("Foto y filtros")},leadingIcon={Icon(Icons.Rounded.AutoAwesome,null,Modifier.size(16.dp))}); FilterChip(selected=tab==1,onClick={tab=1},label={Text("Asistente cámara")},leadingIcon={Icon(Icons.Rounded.Tune,null,Modifier.size(16.dp))}) }
  if(tab==0){
   Spacer(Modifier.height(14.dp)); Text("TU IMAGEN",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp); Spacer(Modifier.height(8.dp))
   val preview=edited?:original
   if(preview!=null) Image(preview.asImageBitmap(),null,Modifier.fillMaxWidth().heightIn(max=430.dp).border(1.dp,if(edited!=null)Gold else Muted,RoundedCornerShape(18.dp))) else Box(Modifier.fillMaxWidth().height(165.dp).background(Panel,RoundedCornerShape(18.dp)).border(1.dp,C(0xFF34363D),RoundedCornerShape(18.dp)),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Rounded.Image,null,tint=Gold,modifier=Modifier.size(34.dp));Spacer(Modifier.height(8.dp));Text("Haz una foto o elige una para empezar",color=C.White,fontWeight=FontWeight.Medium);Text("JPG, PNG y otras imágenes del móvil",color=Muted,fontSize=12.sp)}}
   Spacer(Modifier.height(16.dp)); Text("FILTRO",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp); Spacer(Modifier.height(8.dp)); Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){Filters.forEach{FilterChip(selected=scene==it,onClick={scene=it},label={Text(it,fontSize=12.sp)})}}
   Spacer(Modifier.height(10.dp)); Text("Intensidad del filtro",color=C.White,fontWeight=FontWeight.Medium); Slider(value=strength,onValueChange={strength=it},valueRange=.1f..1f); Text("${(strength*100).toInt()}%",color=Muted,fontSize=12.sp)
   Spacer(Modifier.height(12.dp)); Button(enabled=!busy,onClick={val src=original; if(src==null)Toast.makeText(context,"Primero haz o selecciona una foto",Toast.LENGTH_SHORT).show() else scope.launch{busy=true;edited=withContext(Dispatchers.Default){enhance(src,strength,scene)};busy=false;Toast.makeText(context,"Filtro aplicado",Toast.LENGTH_SHORT).show()}},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp)){if(busy)CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp) else Icon(Icons.Rounded.AutoAwesome,null);Spacer(Modifier.width(8.dp));Text(if(busy)"APLICANDO…" else "APLICAR FILTRO",fontWeight=FontWeight.Bold)}
   if(edited!=null){Spacer(Modifier.height(8.dp));OutlinedButton(onClick={try{val v=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"NASKULL_${System.currentTimeMillis()}.jpg");put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/NASKULL AI Photo")};val u=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);if(u!=null)context.contentResolver.openOutputStream(u)?.use{edited!!.compress(Bitmap.CompressFormat.JPEG,95,it)};Toast.makeText(context,"Guardada en Pictures/NASKULL AI Photo",Toast.LENGTH_LONG).show()}catch(_:Exception){Toast.makeText(context,"No se pudo guardar",Toast.LENGTH_SHORT).show()}},modifier=Modifier.fillMaxWidth().height(50.dp)){Text("GUARDAR FOTO")}}
   Spacer(Modifier.height(12.dp)); Text("Los filtros ajustan color, contraste y tono directamente en el móvil, sin conexión.",color=Muted,fontSize=11.sp,lineHeight=16.sp)
  } else {
   Spacer(Modifier.height(18.dp)); Text("ASISTENTE PARA XIAOMI 17 ULTRA",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){Scenes.forEach{FilterChip(selected=scene==it,onClick={scene=it},label={Text(it,fontSize=12.sp)})}}; Spacer(Modifier.height(12.dp))
   val tips=when(scene){"Comida"->listOf("Usa la cámara principal 1× (23 mm equivalente).","Busca luz lateral suave, por ejemplo junto a una ventana.","Toca el plato para enfocar y reduce algo la exposición si brilla demasiado.","Evita acercarte demasiado para no deformar la comida.");"Retrato"->listOf("Prueba el teleobjetivo óptico 3× si tienes espacio.","Sitúa a la persona cerca de una ventana.","Separa al sujeto del fondo para lograr desenfoque natural.","Evita filtros de belleza intensos.");"Noche"->listOf("Usa la cámara principal 1× para captar más luz.","Apoya el móvil o usa trípode.","Protege las luces brillantes bajando la exposición.","Prueba el modo Noche y evita mucho zoom digital.");"Paisaje"->listOf("Usa 1× para máxima calidad o ultra gran angular para escenas amplias.","Activa la cuadrícula y alinea el horizonte.","En atardeceres, toca el cielo y baja un poco la exposición.","Limpia la lente antes de disparar.");else->listOf("Limpia la lente antes de hacer la foto.","Toca el sujeto principal para enfocar.","Baja un poco la exposición si las luces se queman.","Usa 1× para calidad o el teleobjetivo óptico para detalles lejanos.")}
   tips.forEachIndexed{i,t->Row(Modifier.fillMaxWidth().padding(vertical=8.dp),verticalAlignment=Alignment.Top){Box(Modifier.size(28.dp).background(Gold.copy(alpha=.16f),RoundedCornerShape(9.dp)),contentAlignment=Alignment.Center){Text("${i+1}",color=Gold,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(10.dp));Text(t,color=C.White,fontSize=14.sp,lineHeight=20.sp)}}; Spacer(Modifier.height(8.dp));Button(onClick={takePhoto()},modifier=Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(14.dp)){Icon(Icons.Rounded.CameraAlt,null);Spacer(Modifier.width(8.dp));Text("HACER FOTO AHORA",fontWeight=FontWeight.Bold)}
  }
  Spacer(Modifier.height(24.dp));HorizontalDivider(color=C(0xFF303138));Spacer(Modifier.height(12.dp));Text("NASKULL AI PHOTO  •  Hecho para crear mejores imágenes",color=Muted,fontSize=10.sp)
} } }

@Composable private fun FeatureCard(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier,onClick:()->Unit){Surface(onClick=onClick,modifier=modifier.height(112.dp),color=Panel,shape=RoundedCornerShape(16.dp),border=androidx.compose.foundation.BorderStroke(1.dp,C(0xFF33353B))){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.Center){Icon(icon,null,tint=Gold,modifier=Modifier.size(23.dp));Spacer(Modifier.height(8.dp));Text(title,color=C.White,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(subtitle,color=Muted,fontSize=10.sp)}}}

// Reduce las fotos grandes (p. ej. 50 MP) para no agotar la memoria y corrige la rotación EXIF de la cámara.
private fun loadBitmap(context:Context,uri:Uri,maxSide:Int=3000):Bitmap?{val r=context.contentResolver;val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};r.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,bounds)};var sample=1;while(maxOf(bounds.outWidth,bounds.outHeight)/sample>maxSide)sample*=2;val bmp=r.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply{inSampleSize=sample})}?:return null;val rotation=r.openInputStream(uri)?.use{when(ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)){ExifInterface.ORIENTATION_ROTATE_90->90f;ExifInterface.ORIENTATION_ROTATE_180->180f;ExifInterface.ORIENTATION_ROTATE_270->270f;else->0f}}?:0f;return if(rotation==0f)bmp else Bitmap.createBitmap(bmp,0,0,bmp.width,bmp.height,Matrix().apply{postRotate(rotation)},true)}

private fun enhance(src:Bitmap,amount:Float,scene:String):Bitmap{val w=src.width;val h=src.height;val pixels=IntArray(w*h);src.getPixels(pixels,0,w,0,0,w,h);val contrast=1f+.18f*amount;val brightness=if(scene=="Noche")1.05f else 1.015f;for(i in pixels.indices){val c=pixels[i];var r=((Color.red(c)-128f)*contrast+128f)*brightness;var g=((Color.green(c)-128f)*contrast+128f)*brightness;var b=((Color.blue(c)-128f)*contrast+128f)*brightness;when(scene){
 "Comida"->{r*=1f+.035f*amount;g*=1f+.012f*amount}
 "Paisaje"->{g*=1f+.025f*amount;b*=1f+.02f*amount}
 "Retrato"->r*=1f+.012f*amount
 "B/N"->{val l=.299f*r+.587f*g+.114f*b;val t=.4f+.6f*amount;r+=(l-r)*t;g+=(l-g)*t;b+=(l-b)*t}
 "Vintage"->{val sr=.393f*r+.769f*g+.189f*b;val sg=.349f*r+.686f*g+.168f*b;val sb=.272f*r+.534f*g+.131f*b;val t=.8f*amount;r+=(sr-r)*t+12f*amount;g+=(sg-g)*t+8f*amount;b+=(sb-b)*t+10f*amount}
 "Cálido"->{r*=1f+.09f*amount;g*=1f+.03f*amount;b*=1f-.09f*amount}
 "Frío"->{r*=1f-.07f*amount;b*=1f+.09f*amount}
};pixels[i]=Color.rgb(r.toInt().coerceIn(0,255),g.toInt().coerceIn(0,255),b.toInt().coerceIn(0,255))};return Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also{it.setPixels(pixels,0,w,0,0,w,h)}}
