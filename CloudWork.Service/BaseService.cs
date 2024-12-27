using CloudWork.Service.Interface;
using CloudWork.Repository;
using CloudWork.Repository.Base;
namespace CloudWork.Service
{
    public class BaseService<TEntity> : IBaseService<TEntity> where TEntity : class
    {

        public BaseService(IBaseRepository<TEntity> repository)
        {
            Repository = repository;
        }

        public IBaseRepository<TEntity> Repository { get; set; }

        public async Task<IEnumerable<TEntity>> GetAllAsync()
        {
            return await Repository.GetAllAsync();
        }

        public async Task<TEntity?> GetByIdAsync(int id)
        {
            return await Repository.GetByIdAsync(id);
        }
        /// <summary>
        /// Add entity to database
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task</returns>
        public async Task<int> AddAsync(TEntity entity)
        {
            await Repository.AddAsync(entity);
            return await Repository.SaveAsync();
        }

        /// <summary>
        /// 更新实体到数据库
        /// </summary>
        /// <param name="entity"></param>
        /// <returns>Task 影响行数</returns>
        public async Task<int> UpdateAsync(TEntity entity)
        {
            Repository.Update(entity);
            return await Repository.SaveAsync();
        }

        public async Task<int> DeleteAsync(int id)
        {
            await Repository.DeleteAsync(id);
            return await Repository.SaveAsync();
        }
    }
}
