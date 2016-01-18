/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.bookmark;

import com.khartec.waltz.data.bookmark.BookmarkDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.bookmark.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class BookmarkService {

    private final BookmarkDao bookmarkDao;


    @Autowired
    public BookmarkService(BookmarkDao bookmarkDao) {
        checkNotNull(bookmarkDao, "bookmarkDao must not be null");
        this.bookmarkDao = bookmarkDao;
    }


    public List<Bookmark> findByReference(EntityReference reference) {
        return bookmarkDao.findByReference(reference);
    }


    public Bookmark create(Bookmark bookmark) {
        return bookmarkDao.create(bookmark);
    }


    public Bookmark update(Bookmark bookmark) {
        return bookmarkDao.update(bookmark);
    }


    /**
     *
     * @param id
     * @return true if bookmark deleted
     */
    public boolean deleteById(long id) {
        return bookmarkDao.deleteById(id);
    }


    public Bookmark getById(long bookmarkId) {
        return bookmarkDao.getById(bookmarkId);
    }
}
