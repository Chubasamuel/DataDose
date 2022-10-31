package com.chubasamuel.datadose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chubasamuel.datadose.data.local.*
import com.chubasamuel.datadose.ui.components.AlertCustomPage
import com.chubasamuel.datadose.ui.components.showAlert
import com.chubasamuel.datadose.ui.screens.FillerScreen
import com.chubasamuel.datadose.ui.theme.DataDoseTheme
import com.chubasamuel.datadose.util.DCORPrefs
import com.chubasamuel.datadose.util.LineReader
import com.chubasamuel.datadose.util.XlsUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

var curProjId:Int=-1
private var showDialogForName by mutableStateOf(false)
private var newProjName = "DefaultName"
private var newProjectUri:Uri?=null
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var appDatabase: AppDatabase
    @OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class)
    @Inject lateinit var dcorPrefs: DCORPrefs
    @OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class,
        ExperimentalUnitApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataDoseTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController= rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home"){
                            val projects by remember {
                                derivedStateOf { appDao.getProjects() }
                            }
                            val pp:List<Projects> by projects.collectAsStateWithLifecycle(initialValue = listOf())
                            val isByValue by remember{ derivedStateOf { dcorPrefs.getExportTypeByValue() }}
                            var isChecked by remember{ mutableStateOf(isByValue)}
                            Column(Modifier.fillMaxSize()) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(color = Color(0xFF77BB77))
                                        .padding(vertical = 15.dp), horizontalArrangement = Arrangement.Center){
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
                                    Text("Click here to import a new file",
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { getImportFileUri() }
                                            .padding(vertical = 15.dp)
                                        ,style= TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                                            fontSize = TextUnit(4.5f,
                                            TextUnitType.Em)))

                                            Text("Uncheck this box to export by index (default is by value). You can try both to see which works for you",
                                            Modifier.fillMaxWidth(),style= TextStyle(textAlign = TextAlign.Center, fontStyle = FontStyle.Italic))

                                            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                                                Checkbox(checked = isChecked , onCheckedChange ={
                                                    isChecked=it; dcorPrefs.saveExportTypeByValue(isChecked)
                                                } )
                                            }
                                        }
                                }
                                Homer(pp,{projectId->navController.navigate("filler?project_id=$projectId")},
                                    {projectId-> curProjId=projectId;getExportFileUri()})
                            }
                            if(showDialogForName){
                                var freeText by remember { mutableStateOf("") }
                                showAlert(title = {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally){
                                        Text("Enter a unique name for the project",
                                            style= TextStyle(fontWeight = FontWeight.Bold)
                                        ) }},
                                    text = {
                                        AlertCustomPage(onTextChange ={s->freeText=s;newProjName=s; } ,
                                            freeText = freeText, placeholder = "A unique name..",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                        )
                                    },
                                    onConfirm = {
                                        beginFileImport()
                                       showDialogForName=false
                                    },
                                    onCancel = { showDialogForName = false}
                                )
                            }
                        }
                        composable("filler?project_id={project_id}",
                            arguments = listOf(navArgument("project_id"){type=
                            NavType.IntType})){
                            val projectId by remember{derivedStateOf{it.arguments?.getInt("project_id",1)?:1}}
                            val projectDetail by remember {
                                derivedStateOf { appDao.getProjectDetail(projectId) }
                            }
                           val pp:List<ProjectDetail> by projectDetail.collectAsStateWithLifecycle(initialValue =listOf())
                            FillerScreen(project_id=projectId, tabsCount = 1000,
                                saver= {filled->
                                    CoroutineScope(Dispatchers.IO).launch{
                                    appDao.insertProjectFilled(appDatabase.openHelper.writableDatabase,filled.getSQLInsertQuery()) }},docs=pp,
                                getFilled = appDao::getAllProjectFilledForTab /*{project_id,tab_index->appDao.getAllProjectFilledForTab(project_id, tab_index)}*/
                                )
                        }
                    }

                   }
            }
        }
    }

private fun getImportFileUri(){
    val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type= "text/plain"
    }
    startForResult.launch(intent)
}
private fun getExportFileUri(){
    val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type= "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        putExtra(Intent.EXTRA_TITLE,"Dummy-name.xlsx")
    }
    startForResultExport.launch(intent)
}
private fun beginFileImport(){
    newProjectUri?.let {
        val inp = contentResolver.openInputStream(it)?.bufferedReader() ?: return
        val lineReader = LineReader(inp, appDao)
        lineReader.save(newProjName)
    }
}
private fun beginFileExport(uri: Uri,project_id:Int){
    val os=contentResolver.openOutputStream(uri)?.buffered()?:return
    XlsUtil.exportFilledOptionsToXlsx(this,project_id,os,appDao,dcorPrefs.getExportTypeByValue())
}
    private val startForResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result:ActivityResult->
        val res=result.data
        when(result.resultCode){
            Activity.RESULT_OK->{
                val uri=res?.data
                uri?.let{ newProjectUri=it;showDialogForName=true}
            }
            else->{}
        }
    }
    private val startForResultExport=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result:ActivityResult->
        val res=result.data
        when(result.resultCode){
            Activity.RESULT_OK->{
                val uri=res?.data
                uri?.let{beginFileExport(it,curProjId)}
            }
            else->{}
        }
    }
}

@Composable
private fun Homer(projects: List<Projects>, onClick: (Int) -> Unit, onClickExport: (Int) -> Unit){
    ProjectsList(projects = projects, onClick = onClick, onClickExport = onClickExport)
}
@Composable
private fun ProjectsList(projects:List<Projects>,onClick:(Int)->Unit,onClickExport:(Int)->Unit){
    LazyColumn(Modifier.padding(horizontal=15.dp)){
        items(count=projects.size,key={ind->projects[ind].id?:ind}){
            count->
            ProjectNameCard(project = projects[count], onClick = onClick, onClickExport = onClickExport )
            Spacer(Modifier.height(10.dp))
        }
    }
}
@Composable
private fun ProjectNameCard(project:Projects,onClick:(Int)->Unit,onClickExport:(Int)->Unit){
    Row(
        Modifier
            .fillMaxWidth()
            .background(color=Color(0xFFDDFFDD),shape= RoundedCornerShape(20.dp))
            .padding(8.dp)
            ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(project.title,
            Modifier
                .clickable { onClick(project.id ?: -1) }
                .weight(1f).padding(vertical=10.dp))
        Button(onClick = { onClickExport(project.id?:-1) }) {
            Icon(Icons.Filled.Upload,null)
            Spacer(Modifier.width(5.dp))
            Text("Export")
        }
    }
}
@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DataDoseTheme {
        Greeting("Android")
    }
}