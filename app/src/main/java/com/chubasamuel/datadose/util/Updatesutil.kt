package com.chubasamuel.datadose.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import android.content.pm.PackageInfo
import java.text.SimpleDateFormat
import java.util.*
import com.chubasamuel.datadose.data.remote.GetUpdates
import com.chubasamuel.datadose.data.models.APIModels.DevUpdateAPIModel
import com.chubasamuel.datadose.data.models.APIModels.AppUpdateAPIModel
import com.chubasamuel.datadose.data.models.APIModels.APIUpdates


object UpdatesUtil {
    fun checkForUpdatesFromAPI(getUpdates: GetUpdates, dcorPrefs: DCORPrefs) {
        CoroutineScope(Dispatchers.IO).launch {
            getDevUpdates(getUpdates, dcorPrefs)
            getAppUpdates(getUpdates, dcorPrefs)
        }
    }
    private fun getDevUpdates(getUpdates: GetUpdates, dcorPrefs: DCORPrefs, retry: Int = 3) {
        try {
            val res = getUpdates.getDevUpdate()?.execute()
            if (res?.isSuccessful == true) {
                val data = res.body()
                data?.save(dcorPrefs)
            } else {
                if (retry > 0) {
                    getDevUpdates(getUpdates, dcorPrefs, retry - 1)
                }
            }
        } catch (e: Exception) {
            Log.w("DCOR DEBUG", "Error getting dev update - ${e.message}")
        }
    }

    private fun getAppUpdates(getUpdates: GetUpdates, dcorPrefs: DCORPrefs, retry: Int = 3) {
        try {
            val res = getUpdates.getAppUpdate()?.execute()
            if (res?.isSuccessful == true) {
                val data = res.body()
                data?.save(dcorPrefs)
            } else {
                if (retry > 0) {
                    getAppUpdates(getUpdates, dcorPrefs, retry - 1)
                }
            }
        } catch (e: Exception) {
            Log.w("DCOR DEBUG", "Error getting app dev - ${e.message}")
        }
    }

    private fun DevUpdateAPIModel.save(dcorPrefs: DCORPrefs) {
        dcorPrefs.save( SCONSTS.dev_info_title, this.dev_info_title)
        dcorPrefs.save( SCONSTS.dev_info_version, this.dev_info_version)
        dcorPrefs.save( SCONSTS.dev_start_date, this.dev_start_date)
        dcorPrefs.save( SCONSTS.dev_end_date, this.dev_end_date)
        dcorPrefs.save( SCONSTS.dev_remind_freq, this.dev_remind_freq)
        dcorPrefs.save( SCONSTS.dev_inform_once, this.dev_inform_once)
        dcorPrefs.save( SCONSTS.dev_info, this.dev_info)
        dcorPrefs.save( SCONSTS.dev_btn_okay_text, this.dev_btn_okay_text)
    }

    private fun AppUpdateAPIModel.save(dcorPrefs: DCORPrefs) {
        dcorPrefs.save(
            SCONSTS.update_app_version_code,
            this.update_app_version_code
        )
        dcorPrefs.save(
            SCONSTS.update_app_version_name,
            this.update_app_version_name
        )
        dcorPrefs.save( SCONSTS.update_type, this.update_type)
        dcorPrefs.save( SCONSTS.update_title, this.update_title)
        dcorPrefs.save( SCONSTS.update_info, this.update_info)
        dcorPrefs.save( SCONSTS.update_view_type, this.update_view_type)
        dcorPrefs.save( SCONSTS.update_enforcing, this.update_enforcing)
        dcorPrefs.save(
            SCONSTS.update_enforcing_minversion,
            this.update_enforcing_minversion
        )
        dcorPrefs.save( SCONSTS.update_severity, this.update_severity)
    }

    private fun isAppOutdated(context: Context, dcorPrefs: DCORPrefs): Boolean {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val vCode = PackageInfoCompat.getLongVersionCode(pInfo)
            dcorPrefs.check(
                SCONSTS.update_app_version_code,
                0
            ) > vCode
        } catch (e: Exception) {
            false
        }
    }

    private fun isAppOutdatedAndEnforcingMinimumUpdate(
        context: Context,
        dcorPrefs: DCORPrefs
    ): Boolean {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val vCode = PackageInfoCompat.getLongVersionCode(pInfo)
            dcorPrefs.check(

                SCONSTS.update_enforcing_minversion,
                0
            ) > vCode &&
                    isAppOutdated(context, dcorPrefs) && dcorPrefs.check(

                SCONSTS.update_enforcing,
                false
            )
        } catch (e: Exception) {
            false
        }
    }
    private fun isPastTime(dcorPrefs: DCORPrefs): Boolean {
        val tM = dcorPrefs.check( SCONSTS.update_last_informed, 0L)
        val date = Date()
        val diff: Long = date.time - tM
        return diff / (1000 * 60 * 60 * 24) > 2
    }
    private fun isDevUpdateWithinTime(model: DevUpdateAPIModel): Boolean {
        val dateSDF = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return try {
            val startTimestamp = dateSDF.parse(model.dev_start_date)?.time ?: 0L
            val stopTimestamp = dateSDF.parse(model.dev_end_date)?.time ?: 0L
            val currentTimestamp = Date().time
            currentTimestamp in startTimestamp..stopTimestamp
        } catch (e: Exception) {
            false
        }
    }

    private fun isRightToInformAboutDevUpdate(
        dcorPrefs: DCORPrefs,
        model: DevUpdateAPIModel
    ): Boolean {
        if (model.dev_inform_once && dcorPrefs.check(
                SCONSTS.dev_update_informed_version,
                0
            ) == model.dev_info_version
        ) {
            return false
        }
        if (model.dev_inform_once && dcorPrefs.check(

                SCONSTS.dev_update_informed_version,
                0
            ) != model.dev_info_version
        ) {
            return isDevUpdateWithinTime(model)
        }
        if (!model.dev_inform_once && isDevUpdateWithinTime(model)) {
            val lastInformed = dcorPrefs.check(

                SCONSTS.dev_update_last_informed_timestamp,
                1L
            )
            val currentTimestamp = Date().time
            val currDiff = (currentTimestamp - lastInformed) / (1000 * 60 * 60 * 24)
            val freq = model.dev_remind_freq
            return currDiff == currentTimestamp || currDiff > freq
        }
        return false
    }

    private fun getNormalAppUpdate(
        context: Context,
        dcorPrefs: DCORPrefs,
    ):APIUpdates {
       return if (dcorPrefs.check( SCONSTS.update_view_type, "")
                .lowercase(Locale.getDefault()).trim { it <= ' ' } == "snackbar"
        ) {
            val updateVersionName =    dcorPrefs.check(
                    SCONSTS.update_app_version_name,
                    ""
                )
           val msg=String.format(
               Locale.ENGLISH, "Datadose v%s build %d is available", updateVersionName,
               dcorPrefs.check(

                   SCONSTS.update_app_version_code,
                   0
               )
           )
           APIUpdates.SnackBar(msg)
        } else {
            getAppUpdate(context,dcorPrefs)
        }
    }
    private fun getDevUpdate(dcorPrefs: DCORPrefs):APIUpdates? {
        val model = getDevUpdateFromPref(dcorPrefs)
        return if(isRightToInformAboutDevUpdate(dcorPrefs, model))
        {APIUpdates.NormalAlert(model.dev_info_title,model.dev_info)}
        else{null}
    }
    private fun getAppUpdate(context: Context, dcorPrefs: DCORPrefs):APIUpdates {
     val title= dcorPrefs.check(
                SCONSTS.update_title,
                "App Update Is Available"
            )
        val updateMsg = getUpdateMsg(dcorPrefs)
        return APIUpdates.NormalAlert(title,updateMsg)
    }
  /*  private fun showSnackbarForUpdate(
        context: Context,
        dcorPrefs: DCORPrefs,
        coordinatorLayout: CoordinatorLayout,
        update_version_name: String
    ) {
        if (sb_app_update?.isShown == true) return
        Snackbar.make(
            coordinatorLayout, String.format(
                Locale.ENGLISH, "Pomodoro v%s build %d is available", update_version_name,
                dcorPrefs.check(

                    SCONSTS.update_app_version_code,
                    0
                )
            ), Snackbar.LENGTH_LONG
        ).also {
            sb_app_update = it
            it.setAction("Update") {
                sb_app_update?.dismiss()
                saveLastUpdateInform(dcorPrefs)
                launchPlayStore(context)
            }
            it.show()
        }
    }
*/
   /* private fun showAlertForUpdate(context: Context, dcorPrefs: DCORPrefs, alertStyle: Int) {
        if (alert_app_update?.isShowing == true) return
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(
            context,
            alertStyle
        )
        alertDialog.setTitle(
            dcorPrefs.check(

                SCONSTS.update_title,
                "App Update Is Available"
            )
        )
        val updateMsg = getUpdateMsg(dcorPrefs)
        alertDialog.setMessage(updateMsg)
        alertDialog.setNegativeButton("Not Now") { dialog, _ ->
            dialog.cancel()
            saveLastUpdateInform(dcorPrefs)
        }
        alertDialog.setPositiveButton("Update") { dialogInterface, _ ->
            dialogInterface.cancel()
            saveLastUpdateInform(dcorPrefs)
            launchPlayStore(context)
        }
        alert_app_update = alertDialog.create()
        alert_app_update?.setCanceledOnTouchOutside(false)
        alert_app_update?.setCancelable(false)
        alert_app_update?.show()
    }*/
/*
    private fun getDevUpdate(
        context: Context,
        dcorPrefs: DCORPrefs,
        model: DevUpdateAPIModel,
    ):APIUpdates.NormalAlert {

        alertDialog.setTitle(model.dev_info_title)
        alertDialog.setMessage(model.dev_info)
        alertDialog.setPositiveButton(model.dev_btn_okay_text) { dialog, _ ->
            dcorPrefs.save(

                SCONSTS.dev_update_informed_version,
                model.dev_info_version
            )
            dcorPrefs.save(

                SCONSTS.dev_update_last_informed_timestamp,
                Date().time
            )
            dialog.cancel()
        }
        alert_dev_update = alertDialog.create()
        alert_dev_update?.setCancelable(false)
        alert_dev_update?.setCanceledOnTouchOutside(false)
        alert_dev_update?.show()
    }
*/
    private fun getForcefulAlertUpdate(
        context: Context,
        dcorPrefs: DCORPrefs,
    ):APIUpdates {

       val title = dcorPrefs.check(
                SCONSTS.update_title,
                "Compulsory App Update"
            )
        val updateMsg = getUpdateMsg(dcorPrefs)
    return  APIUpdates.ForceFulAlert(title,updateMsg)
    }

    private fun getUpdateMsg(dcorPrefs: DCORPrefs): String {
        var res = ""
        res += "DataDose version " + dcorPrefs.check(

            SCONSTS.update_app_version_name,
            ""
        )
        res += """ build ${
            dcorPrefs.check(
                
                SCONSTS.update_app_version_code,
                0
            )
        } is now available

"""
        res += """	${dcorPrefs.check( SCONSTS.update_info, "")}

"""
        return res
    }

    private fun getDevUpdateFromPref(dcorPrefs: DCORPrefs): DevUpdateAPIModel {
        return DevUpdateAPIModel(
            dcorPrefs.check(SCONSTS.dev_info_title, ""),
            dcorPrefs.check(SCONSTS.dev_info_version, -1),
            dcorPrefs.check(SCONSTS.dev_start_date, ""),
            dcorPrefs.check(SCONSTS.dev_end_date, ""),
            dcorPrefs.check(SCONSTS.dev_remind_freq, -1),
            dcorPrefs.check(SCONSTS.dev_inform_once, true),
            dcorPrefs.check(SCONSTS.dev_info, ""),
            dcorPrefs.check(SCONSTS.dev_btn_okay_text, "Okay Thanks")
        )
    }

   private fun getAppUpdateHelper(
        context: Context,
        dcorPrefs: DCORPrefs,
    ):APIUpdates? {
        return try {
            if (isAppOutdatedAndEnforcingMinimumUpdate(context, dcorPrefs)) {
                getForcefulAlertUpdate(context, dcorPrefs)
            } else if (isAppOutdated(context, dcorPrefs) && isPastTime(dcorPrefs)) {
                getNormalAppUpdate(context, dcorPrefs)
            }else{
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
  fun saveLastDevUpdateInformed(dcorPrefs: DCORPrefs){
      dcorPrefs.save(
          SCONSTS.dev_update_last_informed_timestamp,
          Date().time
      )
  }
  fun saveLastAppUpdateInformed(dcorPrefs: DCORPrefs) {
        dcorPrefs.save( SCONSTS.update_last_informed, Date().time)
    }
  fun launchPlayStore(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse("http://play.google.com/store/apps/details?id=com.chubasamuel.datadose")
        val chooser = Intent.createChooser(intent, "launch Play store")
        context.startActivity(chooser)
    }
  fun mainGetAppUpdate(context: Context, dcorPrefs: DCORPrefs):APIUpdates? = getAppUpdateHelper(context,dcorPrefs)
  fun mainGetDevUpdate(dcorPrefs: DCORPrefs):APIUpdates? = getDevUpdate(dcorPrefs)
}