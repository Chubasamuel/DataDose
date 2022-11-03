package com.chubasamuel.datadose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chubasamuel.datadose.data.local.*
import com.chubasamuel.datadose.data.models.APIModels
import com.chubasamuel.datadose.data.models.WorkStatus
import com.chubasamuel.datadose.data.remote.GetUpdates
import com.chubasamuel.datadose.ui.components.*
import com.chubasamuel.datadose.ui.screens.FillerScreen
import com.chubasamuel.datadose.ui.theme.DataDoseTheme
import com.chubasamuel.datadose.ui.theme.buttonColor
import com.chubasamuel.datadose.ui.theme.statusBarColor
import com.chubasamuel.datadose.util.DCORPrefs
import com.chubasamuel.datadose.util.LineReader
import com.chubasamuel.datadose.util.UpdatesUtil
import com.chubasamuel.datadose.util.XlsUtil
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var appDatabase: AppDatabase
    @Inject lateinit var dcorPrefs: DCORPrefs
    @Inject lateinit var appContext:Context
    @Inject lateinit var getUpdates: GetUpdates

    var curProjId:Int=-1
    private var showDialogForName by mutableStateOf(false)
    private var newProjName = "DefaultName"
    private var newProjectUri:Uri?=null
    private var workStatus:WorkStatus by mutableStateOf(WorkStatus.Waiting())

    private val coroutineScope=CoroutineScope(Dispatchers.IO)

     private var appUpdateM:APIModels.APIUpdates? by mutableStateOf(null)
     private var devUpdateM:APIModels.APIUpdates? by mutableStateOf(null)
     private fun resetAppUpdate(){appUpdateM=null}
     private fun resetDevUpdate(){devUpdateM=null}

    @OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterialApi::class,
        ExperimentalUnitApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coroutineScope.launch {
            appUpdateM = UpdatesUtil.mainGetAppUpdate(appContext,dcorPrefs)
            devUpdateM = UpdatesUtil.mainGetDevUpdate(dcorPrefs) }
        setContent {
            DataDoseTheme {
                val systemUiController= rememberSystemUiController()
                LaunchedEffect(Unit){
                    systemUiController.setStatusBarColor(color=statusBarColor)
                }
                // A surface container using the 'background' color from the theme
                val scaffoldState= rememberScaffoldState()
                Scaffold(scaffoldState=scaffoldState) {
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
                            val projectsFilledCount = remember { mutableStateMapOf<Int,Int>() }
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
                                Homer(pp,projectsFilledCount,{projectId->navController.navigate("filler?project_id=$projectId")},
                                    {projectId,projectName-> curProjId=projectId;getExportFileUri(projectName)})
                            }
                            var inputNewProjName by remember { mutableStateOf("") }
                            if(showDialogForName){
                                showAlert(title = {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally){
                                        Text("Enter a unique name for the project",
                                            style= TextStyle(fontWeight = FontWeight.Bold)
                                        ) }},
                                    text = {
                                        AlertCustomPage(onTextChange ={s->inputNewProjName=s; newProjName=s; } ,
                                            freeText = inputNewProjName, placeholder = "A unique name..",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                        )
                                    },
                                    onConfirm = {
                                        beginFileImport(coroutineScope)
                                       showDialogForName=false
                                    },
                                    onCancel = { showDialogForName = false}
                                )
                            }
                            val showProgress by remember{derivedStateOf{workStatus is WorkStatus.Working}}
                            if(showProgress){
                                val statusText by remember {derivedStateOf{
                                    val kp=workStatus
                                    if(kp is WorkStatus.Working) kp.statusMessage
                                    else null
                                }}
                                indeterminateProgress(statusText)}
                            AppUpdateComponent(update = appUpdateM,resetVal={resetAppUpdate()},
                                saveLast={UpdatesUtil.saveLastAppUpdateInformed(dcorPrefs)},
                                launchPlayStore = {UpdatesUtil.launchPlayStore(appContext)},
                                showSnackBar={msg->coroutineScope.launch {
                                   val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(message=msg,actionLabel="Update")
                                    when(snackbarResult){
                                        SnackbarResult.ActionPerformed->UpdatesUtil.launchPlayStore(appContext)
                                        else->{}
                                    }
                                } }
                                )
                            DevUpdateComponent(update = devUpdateM,resetVal={resetDevUpdate()},
                                saveLast={UpdatesUtil.saveLastDevUpdateInformed(dcorPrefs)})
                            LaunchedEffect(Unit){
                               coroutineScope.launch{
                                   val filledCountMap = appDao.getAllProjectFilledCount()
                                   projectsFilledCount.putAll(filledCountMap)
                               }
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
                                getFilled = appDao::getAllProjectFilledForTab
                                )
                        }
                    }

                   }
            }
        }}
        checkForApiUpdates(getUpdates, dcorPrefs)
    }
private fun checkForApiUpdates(getUpdates: GetUpdates,dcorPrefs: DCORPrefs){
    UpdatesUtil.checkForUpdatesFromAPI(getUpdates, dcorPrefs)
}
private fun getImportFileUri(){
    val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type= "text/plain"
    }
    startForResult.launch(intent)
}
private fun getExportFileUri(title:String){
    val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type= "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        putExtra(Intent.EXTRA_TITLE,"$title.xlsx")
    }
    startForResultExport.launch(intent)
}
private fun beginFileImport(scope:CoroutineScope){
    newProjectUri?.let {
        val inp = contentResolver.openInputStream(it)?.bufferedReader() ?: return
        val lineReader = LineReader(inp, appDao)
       scope.launch { lineReader.save(newProjName).collect{ it2->workStatus=it2}}
    }
}
private fun beginFileExport( uri: Uri, project_id:Int, scope: CoroutineScope){
    val os=contentResolver.openOutputStream(uri)?.buffered()?:return
    scope.launch{
        XlsUtil.exportFilledOptionsToXlsx(appContext,project_id,os,appDao,dcorPrefs.getExportTypeByValue())
        .collect{
            workStatus=it
        }}
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
                uri?.let{beginFileExport(it,curProjId,coroutineScope)}
            }
            else->{}
        }
    }
}

@Composable
private fun Homer(projects: List<Projects>,projectsFilledCount:Map<Int,Int>, onClick: (Int) -> Unit, onClickExport: (Int,String) -> Unit){
    ProjectsList(projects = projects,projectsFilledCount=projectsFilledCount, onClick = onClick, onClickExport = onClickExport)
}
@Composable
private fun ProjectsList(projects:List<Projects>,projectsFilledCount:Map<Int,Int>,onClick:(Int)->Unit,onClickExport:(Int,String)->Unit){
    LazyColumn(Modifier.padding(horizontal=15.dp)){
        items(count=projects.size,key={ind->projects[ind].id?:ind}){
            count->
            val pFc by remember{derivedStateOf{projectsFilledCount[projects[count].id]?:0}}
            ProjectNameCard(project = projects[count],projectFilledCount=pFc, onClick = onClick, onClickExport = onClickExport )
            Spacer(Modifier.height(10.dp))
        }
    }
}
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProjectNameCard(project:Projects,projectFilledCount:Int,onClick:(Int)->Unit,onClickExport:(Int,String)->Unit){
    val pFc by remember { mutableStateOf(projectFilledCount)}
    val projId by remember {derivedStateOf{project.id ?: -1}}
    Surface(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),shape=RoundedCornerShape(15.dp),color = Color(0xFFDDFFDD),
        onClick = {onClick(projId) }){
    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ){
        Row( horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically){
        Text(project.title,
            Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
        style=TextStyle(fontWeight = FontWeight.Bold))
        Button(onClick = { onClickExport(projId,project.title) }, colors = buttonColor) {
            Icon(Icons.Filled.Upload,null)
            Spacer(Modifier.width(5.dp))
            Text("Export")
        }}
        if(pFc>0){
            val sLabel=if(pFc==1) "response" else "responses"
            Text("$pFc $sLabel filled",modifier=Modifier.fillMaxWidth(),
                style= TextStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            )
        }
    }}
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