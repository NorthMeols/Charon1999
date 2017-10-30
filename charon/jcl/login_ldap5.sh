cat <<+END+ >script
# --------------------------------------------------------------------------------
#  use ldap to check if user is loged in (Encrypted)
# --------------------------------------------------------------------------------
use Net::LDAPS;

\$ldap = Net::LDAPS->new( "ldap.brighton.ac.uk" );
if (! \$ldap) {
   print  "-No LDAP connection (Password server down)\n";
   print  " Central machine ldap.bton.ac.uk not responding\n";
   exit;
}

\$user  =\$ARGV[0];
\$passwd=\$ARGV[1];

\$ldap->bind ( version => 3 );          # use for searches

\$x = \$ldap->start_tls( verify  => "none" );
if (! \$x) {
   print  "-No TLS connection\n";
   print  " Central machine ldap.bton.ac.uk not encrypting\n";
   exit;
}

\$mesg = \$ldap->search(filter => "(uid=\$user)",
                      base   => "ou=People,dc=brighton,dc=ac,dc=uk",
                      attrs => ['dn', 'cn' ] );

\$entry = \$mesg->entry(0);

if ( ! defined \$entry ) {
  print "-User name and/or password invalid\n";
  exit;
}

\$dn = \$entry->dn;



# --------------------------------------------------------------------------------


\$ldap->unbind;


\$ldap = Net::LDAPS->new( "ldap.brighton.ac.uk" );

\$x = \$ldap->start_tls( verify  => "none" );
if (! \$x) {
   print  "-No TLS connection\n";
   print  " Central machine ldap.bton.ac.uk not encrypting\n";
   exit;
}

\$res = \$ldap->bind( \$dn, password => \$passwd );

if ( \$res->code ) {
           print "-User name and/or password invalid\n";
           exit;
       } else {
           print "+Valid login\n";
           exit;
       }

print "-Invalid login\n";

# --------------------------------------------------------------------------------
+END+
set -f
/usr/bin/perl script "$1" "$2" "$3"
