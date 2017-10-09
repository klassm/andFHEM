use File::stat;
use POSIX;

$file = "FHEM/98_gcmsend.pm";

$stat = stat($file);

$size = $stat->size;
$date = POSIX::strftime("%Y-%d-%m", localtime( $stat->mtime ));
$time = POSIX::strftime("%H:%M:%S", localtime( $stat->mtime ));

print "UPD ${date}_${time} $size FHEM/98_gcmsend.pm"
