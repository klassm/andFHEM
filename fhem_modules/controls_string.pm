use File::stat;
use POSIX;

$file = "98_gcmsend.pm";

$stat = stat($file);

$size = $stat->size;
$date = POSIX::strftime("%Y-%d-%m", localtime( $stat->mtime ));
$time = POSIX::strftime("%H:%M:%S", localtime( $stat->mtime ));

print "UPD $date_$time $size https://raw.githubusercontent.com/klassm/andFHEM/master/fhem_modules/98_gcmsend.pm"
