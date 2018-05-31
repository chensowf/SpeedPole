package org.speedpole.mode;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Admin on 2018/5/28.
 */

public class NodeGroup implements Parcelable{

    public String name;
    public String icon;
    public List<Node> nodeList;

    public NodeGroup(String name, String icon, List<Node> nodeList)
    {
        this.name = name;
        this.icon = icon;
        this.nodeList = nodeList;
    }

    protected NodeGroup(Parcel in) {
        name = in.readString();
        icon = in.readString();
        nodeList = in.createTypedArrayList(Node.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeTypedList(nodeList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Node getNode(int index)
    {
        return nodeList.get(index);
    }

    public void addNode(Node node)
    {
        nodeList.add(node);
    }

    public static final Creator<NodeGroup> CREATOR = new Creator<NodeGroup>() {
        @Override
        public NodeGroup createFromParcel(Parcel in) {
            return new NodeGroup(in);
        }

        @Override
        public NodeGroup[] newArray(int size) {
            return new NodeGroup[size];
        }
    };

    public static class Node implements Parcelable
    {
        public String ip;
        public int port;
        public String method;
        public String password;

        public Node(String ip, int port, String method, String password)
        {
            this.ip = ip;
            this.port = port;
            this.method = method;
            this.password = password;
        }

        protected Node(Parcel in) {
            ip = in.readString();
            port = in.readInt();
            method = in.readString();
            password = in.readString();
        }

        public static final Creator<Node> CREATOR = new Creator<Node>() {
            @Override
            public Node createFromParcel(Parcel in) {
                return new Node(in);
            }

            @Override
            public Node[] newArray(int size) {
                return new Node[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(ip);
            dest.writeInt(port);
            dest.writeString(method);
            dest.writeString(password);
        }
    }
}
