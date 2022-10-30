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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chubasamuel.datadose.data.local.*
import com.chubasamuel.datadose.ui.screens.FillerScreen
import com.chubasamuel.datadose.ui.theme.DataDoseTheme
import com.chubasamuel.datadose.util.LineReader
import com.chubasamuel.datadose.util.XlsUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

var curProjId:Int=-1

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var appDao: AppDao
    @Inject lateinit var appDatabase: AppDatabase
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
                            Column(Modifier.fillMaxSize()) {
                                Row(Modifier.clickable { getImportFileUri() }){Greeting("Android")}
                                Homer(appDao,{projectId->navController.navigate("filler?project_id=$projectId")},
                                    {projectId-> curProjId=projectId;getExportFileUri()})
                            }
                        }
                        composable("filler?project_id={project_id}",
                            arguments = listOf(navArgument("project_id"){type=
                            NavType.IntType})){
                            val projectId=it.arguments?.getInt("project_id",1)?:1
                            val projectDetail=appDao.getProjectDetail(projectId)
                           val pp:List<ProjectDetail> by projectDetail.collectAsState(initial =listOf() )
                            FillerScreen(project_id=projectId, tabsCount = 1000,
                                saver={filled->
                                    CoroutineScope(Dispatchers.IO).launch{
                                    appDao.insertProjectFilled(appDatabase.openHelper.writableDatabase,filled.getSQLInsertQuery()) }},docs=pp,
                                getFilled = {project_id,tab_index->appDao.getAllProjectFilledForTab(project_id, tab_index)}
                                )
                        }
                    }

                   }
            }
        }
    }


    /*@Composable
    private fun DoSimple(){
        val asset=this.assets.open("q_mod.txt").bufferedReader()
        val dP=LineReader(asset)
        val r=dP.read()
        r?.let{FillInScreen(docs = r)}
    }*/

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
private fun beginFileImport(uri: Uri){
    val inp=contentResolver.openInputStream(uri)?.bufferedReader()?:return
    val lineReader=LineReader(inp,appDao)
    lineReader.save("DummyName${System.currentTimeMillis()}")
}
private fun beginFileExport(uri: Uri,project_id:Int){
    val os=contentResolver.openOutputStream(uri)?.buffered()?:return
    XlsUtil.exportFilledOptionsToXlsx(this,project_id,os,appDao)
}
    private val startForResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result:ActivityResult->
        val res=result.data
        when(result.resultCode){
            Activity.RESULT_OK->{
                val uri=res?.data
                uri?.let{beginFileImport(it)}
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
private fun Homer(appDao: AppDao,onClick: (Int) -> Unit,onClickExport: (Int) -> Unit){
    val projects=appDao.getProjects()
    val lCo= LocalLifecycleOwner.current
    val projectsAware=remember(projects,lCo){
        projects.flowWithLifecycle(lCo.lifecycle, Lifecycle.State.STARTED)
    }
    val pp:List<Projects> by projectsAware.collectAsState(initial = listOf())
    ProjectsList(projects = pp, onClick = onClick, onClickExport = onClickExport)
}
@Composable
private fun ProjectsList(projects:List<Projects>,onClick:(Int)->Unit,onClickExport:(Int)->Unit){
    LazyColumn{
        items(count=projects.size,key={ind->projects[ind].id?:ind}){
            count->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ){
                Text(projects[count].title,Modifier.clickable { onClick(projects[count].id ?: -1) })
                Button(onClick = { onClickExport(projects[count].id?:-1) }) {
                    Icon(Icons.Filled.Upload,null)
                    Spacer(Modifier.width(5.dp))
                    Text("Export")
                }
            }
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