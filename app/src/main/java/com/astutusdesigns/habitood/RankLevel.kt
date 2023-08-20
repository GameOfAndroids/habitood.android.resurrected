package com.astutusdesigns.habitood

enum class RankLevel {
    FrontlinePersonnel,
    CoreTeamLeader,
    Supervisor,
    BusinessAdmin,
    Owner;

    companion object {
        fun fromInt(level: Int?): RankLevel {
            return when(level) {
                1 -> FrontlinePersonnel
                2 -> CoreTeamLeader
                3 -> Supervisor
                4 -> BusinessAdmin
                5 -> Owner
                else -> FrontlinePersonnel
            }
        }
    }
}