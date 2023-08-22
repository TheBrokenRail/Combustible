package com.thebrokenrail.combustible.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedAdapter;
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

public class CommentFeedAdapterTest {
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

    private static class TestCommentFeedAdapter extends CommentFeedAdapter {
        private TestCommentFeedAdapter() {
            super(null, null, ParentType.POST, -1);
        }

        private List<CommentView> getDataset() {
            return dataset;
        }

        private int getQueuedCommentsSize() {
            return queuedComments.size();
        }

        private void addElements(List<CommentView> elements) {
            addElements(elements, false);
        }
    }

    @Test
    public void testPagination() {
        forEveryTestPost(getCommentsResponses -> {
            TestCommentFeedAdapter allAtOnce = new TestCommentFeedAdapter();
            List<CommentView> allComments = new ArrayList<>();
            for (GetCommentsResponse getCommentsResponse : getCommentsResponses) {
                allComments.addAll(getCommentsResponse.comments);
            }
            allAtOnce.addElements(allComments);

            TestCommentFeedAdapter paginated = new TestCommentFeedAdapter();
            for (GetCommentsResponse getCommentsResponse : getCommentsResponses) {
                paginated.addElements(getCommentsResponse.comments);
            }

            assertEquals(allAtOnce.getQueuedCommentsSize(), paginated.getQueuedCommentsSize());
            List<CommentView> paginatedDataset = paginated.getDataset();
            List<CommentView> allAtOnceDataset = allAtOnce.getDataset();
            assertEquals(allAtOnceDataset.size(), paginatedDataset.size());
            for (int i = 0; i < allAtOnceDataset.size(); i++) {
                assertEquals(allAtOnceDataset.get(i).comment.id, paginatedDataset.get(i).comment.id);
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
            TestCommentFeedAdapter adapter = new TestCommentFeedAdapter();
            for (GetCommentsResponse getCommentsResponse : getCommentsResponses) {
                adapter.addElements(getCommentsResponse.comments);
            }

            List<CommentView> dataset = adapter.getDataset();
            for (int i = 1; i < dataset.size(); i++) {
                CommentView previous = dataset.get(i - 1);
                CommentView current = dataset.get(i);
                String parentPath = stripPath(current.comment.path);
                assertTrue(previous.comment.path.startsWith(parentPath));
            }
        });
    }
}
