package com.thebrokenrail.combustible.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.thebrokenrail.combustible.activity.feed.comment.CommentTreeDataset;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetCommentsResponse;
import com.thebrokenrail.combustible.util.Util;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CommentTreeDatasetTest {
    private void listResources(String dir, Consumer<Path> callback) {
        try {
            URL url = getClass().getResource(dir);
            assertNotNull(url);
            Path path = Paths.get(url.toURI());
            try (Stream<Path> stream = Files.list(path)) {
                stream.forEach(callback);
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readResource(String file) {
        try {
            URL url = getClass().getResource(file);
            assertNotNull(url);
            return new String(Files.readAllBytes(Paths.get(url.toURI())));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void forEveryTestPost(Consumer<List<GetCommentsResponse>> callback) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<GetCommentsResponse> jsonAdapter = moshi.adapter(GetCommentsResponse.class);
        String rootDir = "/posts";
        listResources(rootDir, post -> {
            String postId = post.getFileName().toString();
            if (!postId.endsWith(".sh")) {
                String postDir = rootDir + '/' + postId;
                List<String> pages = new ArrayList<>();
                listResources(postDir, page -> pages.add(page.getFileName().toString()));
                pages.sort((o1, o2) -> {
                    int a = Integer.parseInt(o1.split("\\.")[0]);
                    int b = Integer.parseInt(o2.split("\\.")[0]);
                    return a - b;
                });
                List<GetCommentsResponse> pageObjects = new ArrayList<>();
                for (String page : pages) {
                    String pageFile = postDir + '/' + page;
                    try {
                        GetCommentsResponse pageObject = jsonAdapter.fromJson(readResource(pageFile));
                        assertNotNull(pageObject);
                        assertTrue(pageObject.comments.size() > 0);
                        assertTrue(pageObject.comments.size() <= Util.ELEMENTS_PER_PAGE);
                        pageObjects.add(pageObject);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                callback.accept(pageObjects);
            }
        });
    }

    @Test
    public void testPagination() {
        forEveryTestPost(getCommentsResponses -> {
            CommentTreeDataset allAtOnce = new CommentTreeDataset();
            List<CommentView> allComments = new ArrayList<>();
            for (GetCommentsResponse getCommentsResponse : getCommentsResponses) {
                allComments.addAll(getCommentsResponse.comments);
            }
            allAtOnce.add(null, allComments, false);

            CommentTreeDataset paginated = new CommentTreeDataset();
            for (GetCommentsResponse getCommentsResponse : getCommentsResponses) {
                paginated.add(null, getCommentsResponse.comments, false);
            }

            assertEquals(allAtOnce.getQueuedCommentsSize(), paginated.getQueuedCommentsSize());
            assertEquals(allAtOnce.size(), paginated.size());
            for (int i = 0; i < allAtOnce.size(); i++) {
                assertEquals(allAtOnce.get(i).comment.id, paginated.get(i).comment.id);
            }
        });
    }

    private String stripPath(String path) {
        int index = path.lastIndexOf('.');
        assertNotEquals(-1, index);
        return path.substring(0, index);
    }

    @Test
    public void testSort() {
        forEveryTestPost(getCommentsResponses -> {
            CommentTreeDataset adapter = new CommentTreeDataset();
            for (GetCommentsResponse getCommentsResponse : getCommentsResponses) {
                adapter.add(null, getCommentsResponse.comments, false);
            }

            for (int i = 1; i < adapter.size(); i++) {
                CommentView previous = adapter.get(i - 1);
                CommentView current = adapter.get(i);
                String parentPath = stripPath(current.comment.path);
                assertTrue(previous.comment.path.startsWith(parentPath));
            }
        });
    }

    @Test
    public void testRemove() {
        forEveryTestPost(getCommentsResponses -> {
            CommentTreeDataset adapter = new CommentTreeDataset();
            for (GetCommentsResponse getCommentsResponse : getCommentsResponses) {
                adapter.add(null, getCommentsResponse.comments, false);
            }

            while (true) {
                CommentView commentWithChildren = null;
                for (CommentView comment : adapter) {
                    boolean hasChildren = comment.counts.child_count > 0;
                    boolean isTopLevel = comment.comment.path.equals("0." + comment.comment.id);
                    if (hasChildren && isTopLevel) {
                        commentWithChildren = comment;
                        break;
                    }
                }
                if (commentWithChildren == null) {
                    break;
                }

                int children = 0;
                for (CommentView comment : adapter) {
                    if (comment.comment.path.contains("." + commentWithChildren.comment.id + ".")) {
                        children++;
                    }
                }
                assertTrue(children > 0);

                int expectedSize = adapter.size() - children - 1;
                adapter.remove(null, commentWithChildren);
                assertEquals(expectedSize, adapter.size());
            }

            boolean allTopLevel = true;
            for (CommentView comment : adapter) {
                boolean isTopLevel = comment.comment.path.equals("0." + comment.comment.id);
                if (!isTopLevel) {
                    allTopLevel = false;
                    break;
                }
            }
            assertTrue(allTopLevel);
        });
    }
}
