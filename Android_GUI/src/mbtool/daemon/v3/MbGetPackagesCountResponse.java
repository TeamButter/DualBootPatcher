// automatically generated, do not modify

package mbtool.daemon.v3;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class MbGetPackagesCountResponse extends Table {
  public static MbGetPackagesCountResponse getRootAsMbGetPackagesCountResponse(ByteBuffer _bb) { return getRootAsMbGetPackagesCountResponse(_bb, new MbGetPackagesCountResponse()); }
  public static MbGetPackagesCountResponse getRootAsMbGetPackagesCountResponse(ByteBuffer _bb, MbGetPackagesCountResponse obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public MbGetPackagesCountResponse __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public boolean success() { int o = __offset(4); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public long systemPackages() { int o = __offset(6); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0; }
  public long systemUpdatePackages() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0; }
  public long nonSystemPackages() { int o = __offset(10); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0; }

  public static int createMbGetPackagesCountResponse(FlatBufferBuilder builder,
      boolean success,
      long system_packages,
      long system_update_packages,
      long non_system_packages) {
    builder.startObject(4);
    MbGetPackagesCountResponse.addNonSystemPackages(builder, non_system_packages);
    MbGetPackagesCountResponse.addSystemUpdatePackages(builder, system_update_packages);
    MbGetPackagesCountResponse.addSystemPackages(builder, system_packages);
    MbGetPackagesCountResponse.addSuccess(builder, success);
    return MbGetPackagesCountResponse.endMbGetPackagesCountResponse(builder);
  }

  public static void startMbGetPackagesCountResponse(FlatBufferBuilder builder) { builder.startObject(4); }
  public static void addSuccess(FlatBufferBuilder builder, boolean success) { builder.addBoolean(0, success, false); }
  public static void addSystemPackages(FlatBufferBuilder builder, long systemPackages) { builder.addInt(1, (int)systemPackages, 0); }
  public static void addSystemUpdatePackages(FlatBufferBuilder builder, long systemUpdatePackages) { builder.addInt(2, (int)systemUpdatePackages, 0); }
  public static void addNonSystemPackages(FlatBufferBuilder builder, long nonSystemPackages) { builder.addInt(3, (int)nonSystemPackages, 0); }
  public static int endMbGetPackagesCountResponse(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
};

