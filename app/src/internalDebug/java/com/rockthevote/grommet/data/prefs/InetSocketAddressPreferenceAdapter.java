package com.rockthevote.grommet.data.prefs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.util.Strings;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static java.net.Proxy.Type.HTTP;

public class InetSocketAddressPreferenceAdapter implements Preference.Converter<InetSocketAddress> {

  @NonNull
  @Override
  public InetSocketAddress deserialize(@NonNull String value) {

    assert value != null; // Not called unless value is present.
    String[] parts = value.split(":", 2);
    String host = parts[0];
    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 80;
    return InetSocketAddress.createUnresolved(host, port);
  }

  @NonNull
  @Override
  public String serialize(@NonNull InetSocketAddress address) {
    String host = null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
      host = address.getHostString();
    } else {
      host = address.getHostName();
    }
    int port = address.getPort();

    return host + ":" + port;
  }

  public static @Nullable
  InetSocketAddress parse(@Nullable String value) {
    if (Strings.isBlank(value)) {
      return null;
    }
    String[] parts = value.split(":", 2);
    if (parts.length == 0) {
      return null;
    }
    String host = parts[0];
    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 80;
    return InetSocketAddress.createUnresolved(host, port);
  }

  public static @Nullable
  Proxy createProxy(@Nullable InetSocketAddress address) {
    if (address.getHostString().equals("default")) {
      return null;
    }
    return new Proxy(HTTP, address);
  }
}
