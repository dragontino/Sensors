package com.sensors.app.util

import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoTdscdma
import android.telephony.CellInfoWcdma
import com.sensors.domain.model.Cell
import com.sensors.domain.model.CellType

internal fun Cell(cellInfo: CellInfo): Cell? = when(cellInfo) {
    is CellInfoGsm -> Cell(cellInfo)
    is CellInfoCdma -> Cell(cellInfo)
    is CellInfoLte -> Cell(cellInfo)
    is CellInfoWcdma -> Cell(cellInfo)
    is CellInfoTdscdma -> Cell(cellInfo)
    is CellInfoNr -> Cell(cellInfo)
    else -> null
}



private fun Cell(cellInfo: CellInfoGsm) = Cell(
    type = CellType.GSM,
    id = cellInfo.cellIdentity.cid.toAvailableString(),
    registered = cellInfo.isRegistered,
    mobileNetworkOperator = cellInfo.cellIdentity.mobileNetworkOperator,
    signalStrengthIndicator = cellInfo.cellSignalStrength.dbm.toString(),
    locationAreaCode = cellInfo.cellIdentity.lac.toAvailableString()
)

private fun Cell(cellInfo: CellInfoCdma) = Cell(
    type = CellType.CDMA,
    id = cellInfo.cellIdentity.networkId.toString(),
    registered = cellInfo.isRegistered,
    mobileNetworkOperator = null,
    signalStrengthIndicator = cellInfo.cellSignalStrength.cdmaDbm.toString(),
    locationAreaCode = with(cellInfo.cellIdentity) {
        "lat=$latitude; lon=$longitude; station id = $basestationId"
    }
)

private fun Cell(cellInfo: CellInfoLte) = Cell(
    type = CellType.LTE,
    id = cellInfo.cellIdentity.ci.toAvailableString(),
    registered = cellInfo.isRegistered,
    mobileNetworkOperator = cellInfo.cellIdentity.mobileNetworkOperator,
    signalStrengthIndicator = cellInfo.cellSignalStrength.dbm.toString(),
    locationAreaCode = cellInfo.cellIdentity.tac.toAvailableString()
)

private fun Cell(cellInfo: CellInfoWcdma) = Cell(
    type = CellType.WCDMA,
    id = cellInfo.cellIdentity.cid.toAvailableString(),
    registered = cellInfo.isRegistered,
    mobileNetworkOperator = cellInfo.cellIdentity.mobileNetworkOperator,
    signalStrengthIndicator = cellInfo.cellSignalStrength.dbm.toAvailableString(),
    locationAreaCode = cellInfo.cellIdentity.lac.toAvailableString()
)

private fun Cell(cellInfo: CellInfoTdscdma) = Cell(
    type = CellType.TDSCDMA,
    id = cellInfo.cellIdentity.cid.toAvailableString(),
    registered = cellInfo.isRegistered,
    mobileNetworkOperator = cellInfo.cellIdentity.mobileNetworkOperator,
    signalStrengthIndicator = cellInfo.cellSignalStrength.dbm.toAvailableString(),
    locationAreaCode = cellInfo.cellIdentity.lac.toAvailableString()
)

private fun Cell(cellInfo: CellInfoNr): Cell {
    val identity = cellInfo.cellIdentity as CellIdentityNr
    return Cell(
        type = CellType.NR,
        id = identity.pci.toAvailableString(),
        registered = cellInfo.isRegistered,
        mobileNetworkOperator = arrayOf(
            identity.mccString,
            identity.mncString
        ).filterNotNull().joinToString(""),
        signalStrengthIndicator = cellInfo.cellSignalStrength.dbm.toAvailableString(),
        locationAreaCode = (cellInfo.cellIdentity as CellIdentityNr).tac.toAvailableString()
    )
}


private fun Int.toAvailableString() =
    takeIf { it != CellInfo.UNAVAILABLE }
    ?.toString()
    ?: "Unavailable"