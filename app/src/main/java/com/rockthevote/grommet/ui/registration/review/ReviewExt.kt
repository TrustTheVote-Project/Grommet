package com.rockthevote.grommet.ui.registration.review

import android.graphics.Bitmap
import android.view.View
import com.rockthevote.grommet.data.db.model.FormLanguage
import com.rockthevote.grommet.databinding.FragmentReviewAndConfirmBinding
import com.rockthevote.grommet.util.Images
import java.io.ByteArrayOutputStream
import java.util.*

fun FragmentReviewAndConfirmBinding.toReviewData(): ReviewData {
    val hasReviewAndConfirmInfo = checkboxAgreement.isChecked

    val signature = ByteArrayOutputStream().use { baos ->
        val image = Images.transformAspectRatio(signaturePad.signatureBitmap, 3, 1).let {
            Images.aspectSafeScale(it, 180, 60)
        }

        image.compress(Bitmap.CompressFormat.PNG, 100, baos)

        baos.toByteArray()
    }


    val lang = if ("es" == Locale.getDefault().language) FormLanguage.SPANISH else FormLanguage.ENGLISH

    return ReviewData(
        hasReviewAndConfirmInfo,
        signature,
        lang.toString()
    )
}

fun FragmentReviewAndConfirmBinding.bindReviewData(data: ReviewAndConfirmStateData) {
    with(data) {
        reviewName.text = name
        reviewBirthday.text = birthday
        reviewEmail.text = email
        reviewPhone.text = phone
        reviewRegAddress.text = residentialAddress

        if (mailingAddress != null) {
            reviewMailingAddressDivider.visibility = View.VISIBLE
            reviewMailingAddressSectionTitle.visibility = View.VISIBLE
            reviewMailAddress.visibility = View.VISIBLE
            reviewMailAddress.text = mailingAddress
        } else {
            // Needs to be set to GONE in case data changes
            reviewMailingAddressDivider.visibility = View.GONE
            reviewMailingAddressSectionTitle.visibility = View.GONE
            reviewMailAddress.visibility = View.GONE
            reviewMailAddress.text = ""
        }

        reviewRace.text = race
        reviewParty.text = party
    }
}