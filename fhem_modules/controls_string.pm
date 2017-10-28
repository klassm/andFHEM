use warnings FATAL => 'all';
use strict;
use File::stat;
use POSIX;

my $file = "FHEM/98_gcmsend.pm";

my $stat = stat($file);

my $size = $stat->size;
my $date = POSIX::strftime("%Y-%d-%m", localtime( $stat->mtime ));
my $time = POSIX::strftime("%H:%M:%S", localtime( $stat->mtime ));

print "UPD ${date}_${time} $size FHEM/98_gcmsend.pm"
