* Als generische Geräte migriert: FB_CALLMONITOR, Weather, gcmsend, weblink, FLOORPLAN
* Repariere Absturz bei Drehen des Gerätes während dem Laden von Graphen
* Schließe Suchen-Ansicht wenn Suche abgeschickt wird
* Slider in Detailansicht hat wieder die volle Tabellengröße
* Repariere nextAlarmClock Update für Android Versionen >= Nougat
* Behandle Umbenennungen von Geräten in FHEM richtig, sodass diese in andFHEM anschließend nicht zweimal angezeigt werden
* Geräte mit webCmd on:off konnten nicht mehr als Umschalter geschaltet werden (in AppWidgets un in der App)
* Falls auf lokal kaputten Gerätelisten (die nicht mehr gelesen werden können) ein Teil-Update ausgeführt werden soll aktualisiere stattdessen die komplette Geräteliste
* Werte widgetOverride :noArg Attribut aus um die Widgets zum Togglen bzw. Dimmen von Geräten zu deaktivieren.