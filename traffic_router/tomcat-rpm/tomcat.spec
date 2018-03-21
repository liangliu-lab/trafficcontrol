%define debug_package %{nil}

Name:       tomcat
Version:    %{tomcat_version}
Release:    %{build_number}
Summary:    Apache Tomcat Servlet/JSP Engine 8.5+, RI for Servlet 3.1/JSP 2.3 API
License:    Apache Software License
URL:        https://github.com/apache/incubator-trafficcontrol/
Source:     %{_sourcedir}/apache-tomcat-%{version}.tar.gz
Requires:   jdk >= 1.8

%define tomcat_home /opt/tomcat

%description
This rpm is a minimal install of the Tomcat servlet container version 8.5. 
It gets installed to /opt/tomcat and contains no webapps of its own. 
To use it create your own CATALINA_BASE directory and place your application 
specific webapps and server.xml there. 
You will also need your own systemd unit file for starting your application 
with the correct setting for CATALINA_BASE. 

Built:@BUILT@

%prep
%setup -q -n apache-tomcat-%{version}

%build

%install
install -d -m 755 ${RPM_BUILD_ROOT}/%{tomcat_home}/
cp -R * ${RPM_BUILD_ROOT}/%{tomcat_home}/

# Remove all webapps. 
rm -rf ${RPM_BUILD_ROOT}/%{tomcat_home}/webapps/*

# Remove *.bat
rm -f ${RPM_BUILD_ROOT}/%{tomcat_home}/bin/*.bat

# Drop sysd script
install -d -m 755 ${RPM_BUILD_ROOT}/%{_sysconfdir}/systemd/system
install    -m 755 %_sourcedir/tomcat.service ${RPM_BUILD_ROOT}/%{_sysconfdir}/systemd/system/tomcat.service 

%clean
rm -rf ${RPM_BUILD_ROOT}

%pre
chkconfig tomcat off

if [[ -e /opt/apache-tomcat-* ]]; then
  echo "Deleting unmanaged Tomcat install from < 2.3 version of Traffic Router"
  rm -rf /opt/apache-tomcat-*
  rm -rf /opt/tomcat
fi

%files
%defattr(-,root,root)
%{tomcat_home}
%{_sysconfdir}/systemd/system/tomcat.service 

%post
systemctl daemon-reload
echo "Tomcat for Traffic Router installed successfully."
echo ""
echo "Start with 'sudo systemctl start traffic_router'"

%preun

%postun

%changelog